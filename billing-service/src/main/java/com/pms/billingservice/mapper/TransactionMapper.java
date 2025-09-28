package com.pms.billingservice.mapper;

import com.pms.billingservice.dto.TransactionRequestDTO;
import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.model.BillingAccount;
import com.pms.billingservice.model.Transaction;

import java.math.BigDecimal;

public class TransactionMapper {

    public static TransactionResponseDTO toDto(Transaction transaction) {
        TransactionResponseDTO transactionResponseDTO = new TransactionResponseDTO();

        transactionResponseDTO.setTransactionId(transaction.getId());
        transactionResponseDTO.setAmount(transaction.getAmount());
        transactionResponseDTO.setStatus(transaction.getPaymentStatus());
        transactionResponseDTO.setBillingAccount(transaction.getBillingAccount().getId());
        transactionResponseDTO.setPaymentMethod(transaction.getPaymentMethod());
        transactionResponseDTO.setPaymentGateway(transaction.getPaymentGateway());

        return transactionResponseDTO;
    }

    public static Transaction toModel(TransactionRequestDTO transactionRequestDTO, BillingAccount billingAccount) {
        Transaction transaction = new Transaction();

        transaction.setAmount(BigDecimal.valueOf(transactionRequestDTO.getAmount()));
        transaction.setDescription(transactionRequestDTO.getDescription());
        transaction.setPaymentMethod(transactionRequestDTO.getPaymentMethod());
        transaction.setBillingAccount(billingAccount);
        transaction.setPaymentType(transactionRequestDTO.getPaymentType());
        transaction.setPaymentGateway(transactionRequestDTO.getPaymentGateway());

        return transaction;
    }
}
