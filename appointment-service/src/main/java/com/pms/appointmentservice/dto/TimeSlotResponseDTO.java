package com.pms.appointmentservice.dto;

import com.pms.appointmentservice.enums.TimeSlotStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class TimeSlotReponseDTO {
    private UUID id;
    private UUID doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private TimeSlotStatus status;
}
