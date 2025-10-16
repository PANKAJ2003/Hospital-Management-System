package com.pms.billingservice.dto;

import com.pms.billingservice.enums.PaymentStatus;
import com.pms.billingservice.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionDetailDTO {
    private String id;
    private String description;
    private BigDecimal amount;
    private PaymentStatus status;
    private String timestamp;
    private PaymentType paymentType;
}
