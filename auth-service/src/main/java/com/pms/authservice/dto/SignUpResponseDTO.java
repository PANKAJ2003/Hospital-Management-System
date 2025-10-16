package com.pms.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpResponseDTO {
    private String patientId;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
    private String gender;
    private String token;
}
