package com.pms.billingservice.service.payment;

import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.dto.VerifyPaymentRequestDTO;
import com.pms.billingservice.enums.PaymentMethod;
import com.pms.billingservice.enums.PaymentStatus;
import com.pms.billingservice.mapper.TransactionMapper;
import com.pms.billingservice.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CashPaymentProcessor implements PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(CashPaymentProcessor.class);

    @Override
    public boolean supports(PaymentMethod method) {
        return (method == PaymentMethod.CASH);
    }

    @Override
    public TransactionResponseDTO processPayment(Transaction transaction) {
        log.info("Processing cash payment for transaction: {}", transaction.getId());

        TransactionResponseDTO response = TransactionMapper.toDto(transaction);
        response.setStatus(PaymentStatus.SUCCESS);

        // Create gateway order details for cash payment
        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("order_id", "CASH_" + transaction.getId());
        orderDetails.put("payment_type", "CASH");
        orderDetails.put("amount", transaction.getAmount());
        orderDetails.put("currency", "INR");

        response.setGatewayOrderDetails(orderDetails);

        return response;
    }

    @Override
    public boolean verifyPayment(VerifyPaymentRequestDTO verifyPaymentRequestDTO) {
        // Cash payments are verified manually
        return true;
    }

    @Override
    public String getGatewayName() {
        return "CASH";
    }

    @Override
    public String handleWebhook(String payload, Map<String, String> headers) {
        // Cash payments don't have webhooks
        return null;
    }
}