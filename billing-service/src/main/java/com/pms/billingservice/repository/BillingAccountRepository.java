package com.pms.billingservice.repository;

import com.pms.billingservice.model.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BillingAccountRepository extends JpaRepository<BillingAccount, UUID> {
}
