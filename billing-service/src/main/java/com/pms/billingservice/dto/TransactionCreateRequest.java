package com.pms.billingservice.dto;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class TransactionCreateRequest {
    @Valid
    private TransactionRequestDTO transaction;

    private PaymentDetailsDTO paymentDetails;
}
