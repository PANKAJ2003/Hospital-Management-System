package com.pms.billingservice.service;

import com.pms.billingservice.dto.PaymentDetailsDTO;
import com.pms.billingservice.dto.TransactionRequestDTO;
import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.enums.PaymentStatus;
import com.pms.billingservice.exception.BillingAccountDoesNotExistException;
import com.pms.billingservice.mapper.TransactionMapper;
import com.pms.billingservice.model.BillingAccount;
import com.pms.billingservice.model.Transaction;
import com.pms.billingservice.repository.BillingAccountRepository;
import com.pms.billingservice.repository.TransactionRepository;
import com.pms.billingservice.service.payment.PaymentProcessor;
import com.pms.billingservice.service.payment.PaymentProcessorFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BillingAccountRepository billingAccountRepository;
    private final PaymentProcessorFactory paymentProcessorFactory;

    public TransactionService(TransactionRepository transactionRepository, BillingAccountRepository billingAccountRepository, PaymentProcessorFactory paymentProcessorFactory) {
        this.transactionRepository = transactionRepository;
        this.billingAccountRepository = billingAccountRepository;
        this.paymentProcessorFactory = paymentProcessorFactory;
    }

    private BillingAccount getBillingAccount(UUID id) {
        return billingAccountRepository.findById(id)
                .orElseThrow(() -> new BillingAccountDoesNotExistException("Billing account does not exist with ID: " + id));
    }

    protected Transaction saveTransaction(TransactionRequestDTO transactionRequestDTO, BillingAccount billingAccount) {
        Transaction transaction = TransactionMapper.toModel(transactionRequestDTO, billingAccount);
        return transactionRepository.save(transaction);
    }

    protected Transaction updateTransactionStatus(UUID transactionId, boolean paymentStatus) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found with ID: " + transactionId);
        }
        transaction.get().setPaymentStatus(
                paymentStatus ? PaymentStatus.SUCCESS : PaymentStatus.FAILED
        );

        return transactionRepository.save(transaction.get());
    }

    public TransactionResponseDTO processTransaction(TransactionRequestDTO transactionRequestDTO, PaymentDetailsDTO paymentDetails) {

        BillingAccount billingAccount = getBillingAccount(transactionRequestDTO.getBillingAccountId());

        Transaction transaction = saveTransaction(transactionRequestDTO, billingAccount);

        PaymentProcessor paymentProcessor = paymentProcessorFactory.getPaymentProcessor(transactionRequestDTO.getPaymentMethod());
        boolean isPaymentSuccessful = false;

        try {
            isPaymentSuccessful = paymentProcessor.processPayment(transaction, paymentDetails);
        } catch (Exception e) {
            isPaymentSuccessful = false;
        }

        Transaction updatedTransaction = updateTransactionStatus(transaction.getId(), isPaymentSuccessful);

        return TransactionMapper.toDto(updatedTransaction);
    }

}