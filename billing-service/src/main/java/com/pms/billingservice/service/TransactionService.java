package com.pms.billingservice.service;

import com.pms.billingservice.dto.TransactionRequestDTO;
import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.dto.VerifyPaymentRequestDTO;
import com.pms.billingservice.enums.PaymentGateway;
import com.pms.billingservice.enums.PaymentStatus;
import com.pms.billingservice.exception.BillingAccountDoesNotExistException;
import com.pms.billingservice.exception.PaymentProcessingException;
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
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final BillingAccountRepository billingAccountRepository;
    private final PaymentProcessorFactory paymentProcessorFactory;

    public TransactionService(TransactionRepository transactionRepository,
                              BillingAccountRepository billingAccountRepository,
                              PaymentProcessorFactory paymentProcessorFactory) {
        this.transactionRepository = transactionRepository;
        this.billingAccountRepository = billingAccountRepository;
        this.paymentProcessorFactory = paymentProcessorFactory;
    }

    private BillingAccount getBillingAccount(UUID id) {
        return billingAccountRepository.findById(id)
                .orElseThrow(() -> new BillingAccountDoesNotExistException("Billing account does not exist with ID: " + id));
    }

    private Transaction saveTransaction(TransactionRequestDTO transactionRequestDTO, BillingAccount billingAccount) {
        Transaction transaction = TransactionMapper.toModel(transactionRequestDTO, billingAccount);
        return transactionRepository.save(transaction);
    }

    private Transaction updateTransactionStatus(UUID transactionId, boolean paymentStatus) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found with ID: " + transactionId);
        }

        transaction.get().setPaymentStatus(
                paymentStatus ? PaymentStatus.SUCCESS : PaymentStatus.FAILED
        );

        return transactionRepository.save(transaction.get());
    }

    @Transactional
    public TransactionResponseDTO processTransaction(TransactionRequestDTO transactionRequestDTO) {
        BillingAccount billingAccount = getBillingAccount(transactionRequestDTO.getBillingAccountId());
        Transaction transaction = saveTransaction(transactionRequestDTO, billingAccount);

        PaymentProcessor paymentProcessor = paymentProcessorFactory.getPaymentProcessor(transactionRequestDTO.getPaymentGateway());

        try {
            if (!paymentProcessor.supports(transaction.getPaymentMethod())) {
                throw new PaymentProcessingException("Payment method not supported");
            }

            TransactionResponseDTO orderDTO = paymentProcessor.processPayment(transaction);
            transaction.setGatewayOrderId((String) orderDTO.getGatewayOrderDetails().get("order_id"));
            transactionRepository.save(transaction);

            return orderDTO;
        } catch (Exception e) {
            log.error("Failed to process payment for transaction: {}", transaction.getId(), e);
            throw new PaymentProcessingException("Failed to process payment", e);
        }
    }

    @Transactional
    public TransactionResponseDTO verifyTransaction(VerifyPaymentRequestDTO request) {
        PaymentProcessor paymentProcessor = paymentProcessorFactory
                .getPaymentProcessor(request.getPaymentGateway());

        boolean isValidPayment = paymentProcessor.verifyPayment(request);
        log.info("Payment verification result for order {}: {}", request.getOrderId(), isValidPayment);

        if (!isValidPayment) {
            updateTransactionStatus(request.getTransactionId(), false);
            throw new PaymentProcessingException("Payment verification failed for orderId: " + request.getOrderId());
        }

        Transaction updatedTx = updateTransactionStatus(request.getTransactionId(), true);
        log.info("Payment successful for orderId: {}", request.getOrderId());
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
                }, () -> log.warn("Transaction not found for orderId: {}", orderId));

            } else {
                log.warn("Webhook returned null or invalid result for gateway: {}", gateway);
            }

        } catch (Exception e) {
            log.error("Error handling webhook for gateway: {}", gateway, e);
            throw new PaymentProcessingException("Webhook processing error: " + e.getMessage(), e);
        }
    }
}