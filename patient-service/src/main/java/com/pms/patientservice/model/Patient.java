package com.pms.patientservice.model;

import com.pms.patientservice.enums.BloodGroup;
import com.pms.patientservice.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patient", indexes = {
        @Index(name = "idx_patient_user_id", columnList = "userId")
})
@Getter
@Setter
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private UUID userId;

    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String address;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Column(nullable = false)
    private LocalDate registeredDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @NotNull
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
    @Column(nullable = false, length = 10)
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @NotNull
    @Column(nullable = false)
    private String emergencyContactName;

    @NotNull
    @Size(min = 10, max = 10, message = "Emergency contact number must be exactly 10 digits")
    @Column(nullable = false, length = 10)
    private String emergencyContactNumber;
}