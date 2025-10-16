package com.pms.authservice.mapper;

import com.pms.authservice.dto.SignUpRequestDTO;
import patient.BloodGroup;
import patient.CreatePatientRequest;

public class AuthPatientMapper {

    public static CreatePatientRequest toPatientRequestWithUserId(SignUpRequestDTO request, String userId) {
        CreatePatientRequest patientRequest = CreatePatientRequest.newBuilder()
                .setName(request.getName())
                .setEmail(request.getEmail())
                .setAddress(request.getAddress())
                .setBloodGroup(BloodGroup.valueOf(request.getBloodGroup().name()))
                .setPhoneNumber(request.getPhoneNumber())
                .setEmergencyContactName(request.getEmergencyContactName())
                .setEmergencyContactNumber(request.getEmergencyContactNumber())
                .setDateOfBirth(request.getDateOfBirth().toString())
                .setUserId(userId)
                .build();
    }
}
