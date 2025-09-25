package com.pms.billingservice.service.payment;

import com.pms.billingservice.dto.PaymentDetailsDTO;
import com.pms.billingservice.enums.PaymentMethod;
import com.pms.billingservice.model.Transaction;
import org.springframework.stereotype.Service;

@Service
public class CashPaymentProcessor implements PaymentProcessor {
    @Override
    public boolean supports(String method) {
        return PaymentMethod.CASH.name().equalsIgnoreCase(method);
    }

    @Override
    public boolean processPayment(Transaction transaction, PaymentDetailsDTO paymentDetails) {
        System.out.println("Processing payment with cash");
        return true;
    }
}
