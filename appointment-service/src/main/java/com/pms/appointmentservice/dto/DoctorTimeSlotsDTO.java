package com.pms.appointmentservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DoctorTimeSlotsDTO {
    private String doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String doctorAvatar;
    private BigDecimal doctorPricePerSession;
    private List<TimeSlotResponseDTO> timeSlots;
}
