package com.pms.billingservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDetailsDTO {

    // Card Payment
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate; // MM/YY
    private String cvv;

    // UPI Payment
    private String upiId;
}
