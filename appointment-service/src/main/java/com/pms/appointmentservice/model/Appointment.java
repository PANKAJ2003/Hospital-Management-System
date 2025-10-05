package com.pms.appointmentservice.model;

import com.pms.appointmentservice.enums.AppointmentStatus;
import com.pms.appointmentservice.enums.AppointmentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID appointmentId;

    @Column(nullable = false, updatable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID doctorId;

    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    private AppointmentType type;

    @Size(max = 255, message = "Reason for visit cannot exceed 255 characters")
    private String reasonForVisit;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private BigDecimal amount;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

