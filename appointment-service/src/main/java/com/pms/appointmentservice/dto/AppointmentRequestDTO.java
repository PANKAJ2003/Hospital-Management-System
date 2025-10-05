package com.pms.appointmentservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pms.appointmentservice.enums.AppointmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class AppointmentRequestDTO {

    @NotNull(message = "Patient Id is required")
    private UUID patientId;

    @NotNull(message = "Doctor Id is required")
    private UUID doctorId;

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    private String reasonForVisit;
    private AppointmentType visitType;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;
}
