package com.pms.billingservice.dto;

import com.pms.billingservice.enums.PaymentGateway;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VerifyPaymentRequestDTO {

    @NotNull
    private UUID transactionId;
    @NotNull
    private PaymentGateway paymentGateway;
    private String paymentId;
    private String orderId;
    private String signature;
}
