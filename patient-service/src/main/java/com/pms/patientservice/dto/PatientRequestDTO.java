package com.pms.patientservice.dto;

import com.pms.patientservice.dto.validators.CreatePatientValidationGroup;
import com.pms.patientservice.enums.BloodGroup;
import com.pms.patientservice.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PatientRequestDTO {

    @NotNull(message = "User Id is required")
    private UUID userId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Phone number is required")
    @Size(max = 10, message = "Phone number cannot exceed 10 characters")
    private String phoneNumber;

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotNull(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotNull(message = "Emergency contact number is required")
    @Size(max = 10, message = "Emergency contact number cannot exceed 10 characters")
    private String emergencyContactNumber;

    @NotNull(groups = CreatePatientValidationGroup.class, message = "Registered date is required")
    private LocalDate registeredDate;
}
