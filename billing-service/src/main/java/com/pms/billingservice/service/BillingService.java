package com.pms.billingservice.service;

import com.pms.billingservice.model.BillingAccount;
import com.pms.billingservice.repository.BillingAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
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

    @Transactional
    public String findBillingAccountIdByPatientId(String patientId) {
        Optional<BillingAccount> billingAccountOptional = billingAccountRepository.findByPatientId(UUID.fromString(patientId));
        return billingAccountOptional.map(billingAccount -> billingAccount.getId().toString()).orElse(null);
    }

    @Transactional
    public Optional<BillingAccount> updateBillingAccountAmount(String accountId, long amount, boolean isCredit) {
        Optional<BillingAccount> billingAccountOptional = billingAccountRepository.findById(UUID.fromString(accountId));
        if (billingAccountOptional.isEmpty()) {
            throw new IllegalArgumentException("Billing account not found with ID: " + accountId);
        }
        BillingAccount billingAccount = billingAccountOptional.get();
        if (isCredit) {
            BigDecimal newBalance = billingAccount.getBalance().add(BigDecimal.valueOf(amount));
            billingAccount.setBalance(newBalance);
        } else {
            BigDecimal newBalance = billingAccount.getBalance().subtract(BigDecimal.valueOf(amount));
            billingAccount.setBalance(newBalance);
        }

        try {
            billingAccountRepository.save(billingAccount);
            return Optional.of(billingAccount);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
