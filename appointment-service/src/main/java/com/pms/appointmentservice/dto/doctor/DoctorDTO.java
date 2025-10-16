package com.pms.appointmentservice.dto.doctor;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class DoctorDTO {
    private UUID doctorId;
    private String name;
    private String specialty;
    private BigDecimal pricePerSession;
    private String email;
    private String phone;
    private String avatar;
    private String qualifications;
    private Integer experienceMonths;
}