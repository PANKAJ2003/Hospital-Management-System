package com.pms.appointmentservice.dto;

import com.pms.appointmentservice.enums.AppointmentStatus;
import com.pms.appointmentservice.enums.AppointmentType;
import com.pms.appointmentservice.model.Appointment;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link Appointment}
 */
@Value
public class AppointmentResponseDTO implements Serializable {
    UUID appointmentId;
    UUID patientId;
    UUID doctorId;
    LocalDateTime appointmentDateTime;
    AppointmentStatus status;
    AppointmentType type;
    String reasonForVisit;
    LocalDateTime createdAt;
    BigDecimal amount;
}
