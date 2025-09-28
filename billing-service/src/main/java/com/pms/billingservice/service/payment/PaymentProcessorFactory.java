package com.pms.billingservice.service.payment;

import com.pms.billingservice.enums.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessorFactory {
    private final PaymentProcessor cashPaymentProcessor;
    private final PaymentProcessor razorpayPaymentProcessor;

    public PaymentProcessorFactory(PaymentProcessor cashPaymentProcessor, PaymentProcessor razorpayPaymentProcessor) {
        this.cashPaymentProcessor = cashPaymentProcessor;
        this.razorpayPaymentProcessor = razorpayPaymentProcessor;
    }

    public PaymentProcessor getPaymentProcessor(PaymentGateway gateway) {
        if (gateway == null) {
            return cashPaymentProcessor;
        }
        return switch (gateway) {
            case RAZORPAY -> razorpayPaymentProcessor;
            default -> cashPaymentProcessor;
        };
    }
}
