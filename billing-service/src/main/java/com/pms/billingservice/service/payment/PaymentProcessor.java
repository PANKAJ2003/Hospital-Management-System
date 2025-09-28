package com.pms.billingservice.service.payment;

import com.pms.billingservice.dto.TransactionResponseDTO;
import com.pms.billingservice.dto.VerifyPaymentRequestDTO;
import com.pms.billingservice.enums.PaymentMethod;
import com.pms.billingservice.model.Transaction;

import java.util.Map;

public interface PaymentProcessor {

    boolean supports(PaymentMethod method);

    TransactionResponseDTO processPayment(Transaction transaction);

    boolean verifyPayment(VerifyPaymentRequestDTO request);

    default String handleWebhook(String payload, Map<String, String> headers) {
        return null;
    }

    default boolean verifyWebhookSignature(String payload, String signature, Map<String, String> headers) {
        return false;
    }

    default String getOrderId() {
        return null;
    }

    String getGatewayName();
}