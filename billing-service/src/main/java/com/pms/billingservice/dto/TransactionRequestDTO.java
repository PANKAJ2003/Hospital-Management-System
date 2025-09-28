package com.pms.billingservice.dto;

import com.pms.billingservice.enums.PaymentGateway;
import com.pms.billingservice.enums.PaymentMethod;
import com.pms.billingservice.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TransactionRequestDTO {

    @NotNull(message = "Billing ID is required")
    private UUID billingAccountId;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Description is required")
    @Size(max = 256, message = "Description cannot exceed 255 characters")
    private String description;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    private PaymentGateway paymentGateway;
}
