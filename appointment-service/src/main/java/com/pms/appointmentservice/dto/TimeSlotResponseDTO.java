package com.pms.appointmentservice.dto;

import com.pms.appointmentservice.enums.TimeSlotStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TimeSlotResponseDTO {
    private UUID id;
    private UUID doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private TimeSlotStatus status;

    public TimeSlotResponseDTO(UUID id, LocalDate date, LocalTime startTime, LocalTime endTime, TimeSlotStatus status) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}
