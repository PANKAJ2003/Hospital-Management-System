package com.pms.billingservice.dto;

import com.pms.billingservice.enums.PaymentGateway;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VerifyPaymentRequestDTO {

    @NotNull(message = "Transaction ID is required")
    private UUID transactionId;

    @NotNull(message = "Payment gateway is required")
    private PaymentGateway paymentGateway;

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Signature is required")
    private String signature;
}
