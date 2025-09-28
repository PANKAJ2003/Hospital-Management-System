package com.pms.billingservice.service.payment;

import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.dto.VerifyPaymentRequestDTO;
import com.pms.billingservice.enums.PaymentMethod;
import com.pms.billingservice.enums.PaymentStatus;
import com.pms.billingservice.mapper.TransactionMapper;
import com.pms.billingservice.model.Transaction;
import org.springframework.stereotype.Service;

@Service
public class CashPaymentProcessor implements PaymentProcessor {
    @Override
    public boolean supports(PaymentMethod method) {
        return (method == PaymentMethod.CASH);
    }

    @Override
    public TransactionResponseDTO processPayment(Transaction transaction) {
        TransactionResponseDTO response = TransactionMapper.toDto(transaction);
        response.setStatus(PaymentStatus.SUCCESS);
        return response;
    }

    @Override
    public boolean verifyPayment(VerifyPaymentRequestDTO verifyPaymentRequestDTO) {
        return false;
    }

    @Override
    public String getGatewayName() {
        return "";
    }
}
