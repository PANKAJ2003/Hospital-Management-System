package com.pms.billingservice.service.payment;

import com.pms.billingservice.dto.PaymentDetailsDTO;
import com.pms.billingservice.model.Transaction;

public interface PaymentProcessor {
    boolean supports(String method);

    boolean processPayment(Transaction transaction, PaymentDetailsDTO paymentDetails);
}
