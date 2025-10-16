package com.pms.billingservice.controller;

import com.pms.billingservice.dto.BillingAccountDetailResponse;
import com.pms.billingservice.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/billing")
public class  BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<BillingAccountDetailResponse> getBillingDetails(@PathVariable UUID patientId) {
        return ResponseEntity.ok().body(billingService.getBillingAccountByPatientId(patientId));
    }
}
