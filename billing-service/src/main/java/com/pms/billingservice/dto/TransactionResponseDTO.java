package com.pms.billingservice.dto;

import com.pms.billingservice.enums.PaymentGateway;
import com.pms.billingservice.enums.PaymentMethod;
import com.pms.billingservice.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
public class TransactionResponseDTO {
    Map<String, Object> gatewayOrderDetails;
    private UUID transactionId;
    private BigDecimal amount;
    private UUID billingAccount;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private PaymentGateway paymentGateway;
}
