package com.pms.billingservice.service;

import com.pms.billingservice.dto.*;
import com.pms.billingservice.enums.PaymentGateway;
import com.pms.billingservice.enums.PaymentMethod;
import com.pms.billingservice.enums.PaymentStatus;
import com.pms.billingservice.enums.PaymentType;
import com.pms.billingservice.exception.BillingAccountDoesNotExistException;
import com.pms.billingservice.exception.PaymentProcessingException;
import com.pms.billingservice.exception.TransactionNotFound;
import com.pms.billingservice.grpc.AppointmentServiceGrpcClient;
import com.pms.billingservice.mapper.TransactionMapper;
import com.pms.billingservice.model.BillingAccount;
import com.pms.billingservice.model.Transaction;
import com.pms.billingservice.repository.BillingAccountRepository;
import com.pms.billingservice.repository.TransactionRepository;
import com.pms.billingservice.service.payment.PaymentProcessor;
import com.pms.billingservice.service.payment.PaymentProcessorFactory;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final BillingAccountRepository billingAccountRepository;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final AppointmentServiceGrpcClient appointmentServiceGrpcClient;

    public TransactionService(TransactionRepository transactionRepository,
                              BillingAccountRepository billingAccountRepository,
                              PaymentProcessorFactory paymentProcessorFactory,
                              AppointmentServiceGrpcClient appointmentServiceGrpcClient) {
        this.transactionRepository = transactionRepository;
        this.billingAccountRepository = billingAccountRepository;
        this.paymentProcessorFactory = paymentProcessorFactory;
        this.appointmentServiceGrpcClient = appointmentServiceGrpcClient;
    }

    private Transaction updateTransactionStatus(UUID transactionId, boolean paymentStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + transactionId));

        transaction.setPaymentStatus(paymentStatus ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public TransactionResponseDTO processTransaction(TransactionRequestDTO dto, String role) {

        if (role == null || role.isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }

        // Enforce payment type for patients
        if ("PATIENT".equalsIgnoreCase(role)) {
            dto.setPaymentType(PaymentType.PAYMENT);
        }

        // ✅ Idempotency: check if transaction already exists for this appointment + patient + type
        Optional<Transaction> existingTx = transactionRepository
                .findByAppointmentIdAndBillingAccountPatientIdAndPaymentType(
                        dto.getAppointmentId(), dto.getPatientId(), dto.getPaymentType());

        if (existingTx.isPresent()) {
            log.info("Transaction already exists for appointment {} and patient {}. Returning existing transaction.",
                    dto.getAppointmentId(), dto.getPatientId());
            return TransactionMapper.toDto(existingTx.get());
        }

        // Fetch appointment amount
        long amount = appointmentServiceGrpcClient
                .getAppointment(dto.getAppointmentId().toString())
                .getAmount();

        // Fetch billing account
        BillingAccount billingAccount = billingAccountRepository.findByPatientId(dto.getPatientId())
                .orElseThrow(() -> new BillingAccountDoesNotExistException(
                        "Billing account not found for patientId: " + dto.getPatientId()));

        // Map to Transaction entity
        Transaction transaction = TransactionMapper.toModel(dto, billingAccount);
        transaction.setAmount(BigDecimal.valueOf(amount));

        PaymentProcessor paymentProcessor = paymentProcessorFactory.getPaymentProcessor(dto.getPaymentGateway());

        if (!paymentProcessor.supports(transaction.getPaymentMethod())) {
            throw new PaymentProcessingException("Payment method not supported");
        }

        TransactionResponseDTO orderDTO;

        try {
            if (transaction.getPaymentMethod() == PaymentMethod.CASH) {
                // --- Cash Flow ---
                transaction.setPaymentStatus(PaymentStatus.PENDING);
                transactionRepository.save(transaction);

                try {
                    appointmentServiceGrpcClient.updateAppointmentStatus(
                            dto.getAppointmentId().toString(),
                            transaction.getId().toString(),
                            false,  // paymentSuccess
                            PaymentMethod.CASH.name()
                    );

                    transaction.setPaymentStatus(PaymentStatus.PENDING);
                    transactionRepository.save(transaction);
                    billingAccount.setBalance(billingAccount.getBalance().add(BigDecimal.valueOf(amount)));
                    billingAccountRepository.save(billingAccount);


                } catch (Exception e) {
                    log.error("Failed to update appointment for cash payment. Transaction pending.", e);
                    transaction.setPaymentStatus(PaymentStatus.PENDING);
                    transactionRepository.save(transaction);
                    throw new PaymentProcessingException("Failed to update appointment for cash payment.", e);
                }

                orderDTO = TransactionMapper.toDto(transaction);

            } else {
                // --- Online Flow ---
                orderDTO = paymentProcessor.processPayment(transaction);
                transaction.setGatewayOrderId((String) orderDTO.getGatewayOrderDetails().get("order_id"));
                Transaction savedTransaction = transactionRepository.save(transaction);
                orderDTO.setTransactionId(savedTransaction.getId());
            }

            return orderDTO;

        } catch (Exception e) {
            log.error("Failed to process payment for transaction: {}", transaction.getId(), e);
            throw new PaymentProcessingException("Failed to process payment", e);
        }
    }


    @Transactional
    public TransactionResponseDTO verifyTransaction(VerifyPaymentRequestDTO request) {
        Transaction txn = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new TransactionNotFound("Transaction not found"));

        // Idempotency: skip if already processed
        if (txn.getPaymentStatus() == PaymentStatus.SUCCESS) {
            log.info("Transaction {} already successful, skipping verification", txn.getId());
            return TransactionMapper.toDto(txn);
        }
        if (txn.getPaymentStatus() == PaymentStatus.FAILED) {
            log.info("Transaction {} already failed, skipping verification", txn.getId());
            return TransactionMapper.toDto(txn);
        }

        PaymentProcessor paymentProcessor = paymentProcessorFactory
                .getPaymentProcessor(request.getPaymentGateway());

        boolean isValidPayment = paymentProcessor.verifyPayment(request);
        log.info("Payment verification result for order {}: {}", request.getOrderId(), isValidPayment);

        if (!isValidPayment) {
            updateTransactionStatus(txn.getId(), false);
            throw new PaymentProcessingException("Payment verification failed for orderId: " + request.getOrderId());
        }

        Transaction updatedTx = updateTransactionStatus(txn.getId(), true);
        log.info("Payment successful for orderId: {}", request.getOrderId());

        // Update appointment after successful online payment
        try {
            appointmentServiceGrpcClient.updateAppointmentStatus(
                    updatedTx.getAppointmentId().toString(),
                    updatedTx.getId().toString(),
                    true,
                    updatedTx.getPaymentMethod().name()
            );
        } catch (Exception e) {
            log.error("Failed to update appointment status after payment verification: {}", e.getMessage());
        }

        return TransactionMapper.toDto(updatedTx);
    }


    public void handleWebhook(String gateway, String payload, Map<String, String> headers) {
        log.info("Processing webhook for gateway: {}", gateway);
        PaymentGateway paymentGateway = PaymentGateway.valueOf(gateway.toUpperCase());

        try {
            PaymentProcessor processor = paymentProcessorFactory.getPaymentProcessor(paymentGateway);
            String result = processor.handleWebhook(payload, headers);

            if (result != null && result.contains(":")) {
                String[] parts = result.split(":");
                String orderId = parts[0];
                PaymentStatus status = "SUCCESS".equals(parts[1]) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

                transactionRepository.findByGatewayOrderId(orderId).ifPresentOrElse(transaction -> {
                    transaction.setPaymentStatus(status);
                    transactionRepository.save(transaction);
                    log.info("Webhook processed successfully for orderId: {} status: {}", orderId, status);

                    // Update appointment after successful payment
                    if (status == PaymentStatus.SUCCESS) {
                        try {
                            appointmentServiceGrpcClient.updateAppointmentStatus(
                                    transaction.getAppointmentId().toString(),
                                    transaction.getId().toString(),
                                    true,
                                    transaction.getPaymentGateway().name()
                            );
                        } catch (Exception e) {
                            log.error("Failed to update appointment status from webhook: {}", e.getMessage());
                        }
                    }
                    if (status == PaymentStatus.FAILED) {
                        try {
                            appointmentServiceGrpcClient.updateAppointmentStatus(
                                    transaction.getAppointmentId().toString(),
                                    transaction.getId().toString(),
                                    false,
                                    transaction.getPaymentGateway().name()
                            );
                        } catch (Exception e) {
                            log.error("Failed to update appointment status from webhook: {}", e.getMessage());
                        }
                    }
                }, () -> log.warn("Transaction not found for orderId: {}", orderId));

            } else {
                log.warn("Webhook returned null or invalid result for gateway: {}", gateway);
            }

        } catch (Exception e) {
            log.error("Error handling webhook for gateway: {}", gateway, e);
            throw new PaymentProcessingException("Webhook processing error: " + e.getMessage(), e);
        }
    }


    public TransactionListResponseDTO getAllTransactionsByPatientId(UUID patientId, int page, int size) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }

        // Find billing account by patient ID
        BillingAccount billingAccount = billingAccountRepository
                .findByPatientId(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Billing account not found for patientId: " + patientId));

        // Create pageable with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        // Fetch transactions by billing account ID (paginated)
        Page<Transaction> transactionPage = transactionRepository
                .findByBillingAccount_Id(billingAccount.getId(), pageable);

        // Convert entity list → DTOs
        List<TransactionDetailDTO> transactionDTOs = transactionPage.getContent()
                .stream()
                .map(TransactionMapper::toTransactionDetailDto)
                .toList();

        // Wrap results into TransactionListResponseDTO
        return new TransactionListResponseDTO(
                transactionDTOs,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }

    public TransactionResponseDTO getTransaction(UUID transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFound("Transaction not found with ID: " + transactionId));

        return TransactionMapper.toDto(transaction);
    }


    @Scheduled(fixedRate = 2 * 60 * 1000)
    public void cleanupPendingPayments() {
        log.info("Checking for pending payments that timed out");
        List<Transaction> pendingTxns = transactionRepository
                .findByPaymentStatusAndTimestampBefore(PaymentStatus.PENDING, LocalDateTime.now().minusMinutes(5));

        for (Transaction txn : pendingTxns) {
            txn.setPaymentStatus(PaymentStatus.FAILED);
            transactionRepository.save(txn);

            // Release appointment slot
            appointmentServiceGrpcClient.updateAppointmentStatus(
                    txn.getAppointmentId().toString(),
                    txn.getId().toString(),
                    false,
                    txn.getPaymentGateway().name()
            );

            log.info("Pending payment timed out. Transaction {} marked FAILED and slot released.", txn.getId());
        }
    }

}