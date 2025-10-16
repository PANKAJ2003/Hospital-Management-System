package com.pms.billingservice.repository;

import com.pms.billingservice.enums.PaymentStatus;
import com.pms.billingservice.enums.PaymentType;
import com.pms.billingservice.model.Transaction;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByGatewayOrderId(String gatewayOrderId);

    Page<Transaction> findByBillingAccount_Id(UUID id, Pageable pageable);

    Optional<Transaction> findByAppointmentIdAndBillingAccountPatientIdAndPaymentType(@NotNull(message = "Appointment ID is required") UUID appointmentId, @NotNull(message = "Patient ID is required") UUID patientId, @NotNull(message = "Payment type is required") PaymentType paymentType);

    List<Transaction> findByPaymentStatusAndTimestampBefore(PaymentStatus paymentStatus, LocalDateTime timestamp);
}
