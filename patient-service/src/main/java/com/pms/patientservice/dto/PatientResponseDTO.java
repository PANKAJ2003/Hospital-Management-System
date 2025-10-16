package com.pms.patientservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientResponseDTO {
    private String id;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
    private String gender;
    private String userId;
    private String phoneNumber;
    private String bloodGroup;
    private String emergencyContactName;
    private String emergencyContactNumber;
}
