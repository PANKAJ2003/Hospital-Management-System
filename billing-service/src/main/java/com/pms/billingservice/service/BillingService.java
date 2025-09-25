package com.pms.billingservice.service;

import com.pms.billingservice.model.BillingAccount;
import com.pms.billingservice.repository.BillingAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BillingService {

    private final BillingAccountRepository billingAccountRepository;

    public BillingService(BillingAccountRepository billingAccountRepository) {
        this.billingAccountRepository = billingAccountRepository;
    }

    @Transactional
    public BillingAccount createBillingAccount(UUID patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        BillingAccount billingAccount = new BillingAccount();
        billingAccount.setPatientId(patientId);
        return billingAccountRepository.save(billingAccount);
    }

}
