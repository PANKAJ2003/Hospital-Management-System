package com.pms.billingservice.service.payment;

import com.pms.billingservice.enums.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessorFactory {
    private final PaymentProcessor cashPaymentProcessor;
    private final PaymentProcessor cardPaymentProcessor;
    private final PaymentProcessor upiPaymentProcessor;

    public PaymentProcessorFactory(PaymentProcessor cashPaymentProcessor, PaymentProcessor cardPaymentProcessor, PaymentProcessor upiPaymentProcessor) {
        this.cashPaymentProcessor = cashPaymentProcessor;
        this.cardPaymentProcessor = cardPaymentProcessor;
        this.upiPaymentProcessor = upiPaymentProcessor;
    }

    public PaymentProcessor getPaymentProcessor(PaymentMethod method) {
        return switch (method) {
            case CASH -> cashPaymentProcessor;
            case DEBIT_CARD, CREDIT_CARD -> cardPaymentProcessor;
            case UPI -> upiPaymentProcessor;
        };
    }
}
