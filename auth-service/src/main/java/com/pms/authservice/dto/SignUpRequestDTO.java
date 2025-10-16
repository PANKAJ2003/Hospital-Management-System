package com.pms.authservice.dto;

import com.pms.authservice.enums.BloodGroup;
import com.pms.authservice.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class SignUpRequestDTO {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password should be at least 6 characters")
    private String password;

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

    @NotNull(message = "Emergency contact number is required")
    @Size(max = 10, message = "Emergency contact number cannot exceed 10 characters")
    private String emergencyContactNumber;

    @NotNull(message = "Emergency contact name is required")
    private String emergencyContactName;

    private LocalDate registeredDate;
}
