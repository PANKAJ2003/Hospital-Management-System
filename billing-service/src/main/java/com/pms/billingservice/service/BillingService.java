package com.pms.billingservice.service;

import com.pms.billingservice.dto.BillingAccountDetailResponse;
import com.pms.billingservice.dto.TransactionDetailDTO;
import com.pms.billingservice.dto.TransactionListResponseDTO;
import com.pms.billingservice.mapper.TransactionMapper;
import com.pms.billingservice.model.BillingAccount;
import com.pms.billingservice.model.Transaction;
import com.pms.billingservice.repository.BillingAccountRepository;
import com.pms.billingservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillingService {

    private final BillingAccountRepository billingAccountRepository;
    private final TransactionRepository transactionRepository;

    public BillingService(BillingAccountRepository billingAccountRepository, TransactionRepository transactionRepository) {
        this.billingAccountRepository = billingAccountRepository;
        this.transactionRepository = transactionRepository;
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

    @Transactional
    public BillingAccountDetailResponse getBillingAccountByPatientId(UUID patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        BillingAccount billingAccount = billingAccountRepository.findByPatientId(patientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Billing account not found for patientId: " + patientId));

        BillingAccountDetailResponse response = new BillingAccountDetailResponse();
        response.setBalance(billingAccount.getBalance());
        response.setStatus(billingAccount.getAccountStatus());
        response.setPatientId(billingAccount.getPatientId().toString());
        return response;
    }

}
