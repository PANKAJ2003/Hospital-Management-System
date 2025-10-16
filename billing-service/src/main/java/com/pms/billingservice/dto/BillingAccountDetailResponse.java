package com.pms.billingservice.dto;

import com.pms.billingservice.enums.AccountStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BillingAccountDetailResponse {
    private String patientId;
    private BigDecimal balance;
    private AccountStatus status;
}
