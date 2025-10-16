package com.pms.authservice.mapper;

import com.pms.authservice.dto.SignUpRequestDTO;
import com.pms.authservice.dto.UserDTO;
import patient.AuthPatientResponse;
import patient.BloodGroup;
import patient.CreatePatientRequest;
import patient.Gender;

public class AuthPatientResponseMapper {

    public static CreatePatientRequest toPatientRequestWithUserId(SignUpRequestDTO request, String userId) {
        return CreatePatientRequest.newBuilder()
                .setName(request.getName())
                .setEmail(request.getEmail())
                .setAddress(request.getAddress())
                .setBloodGroup(BloodGroup.valueOf(request.getBloodGroup().name()))
                .setPhoneNumber(request.getPhoneNumber())
                .setEmergencyContactName(request.getEmergencyContactName())
                .setEmergencyContactNumber(request.getEmergencyContactNumber())
                .setDateOfBirth(request.getDateOfBirth().toString())
                .setUserId(userId)
                .setGender(Gender.valueOf(request.getGender().name()))
                .build();
    }

    public static UserDTO toUserDTO(AuthPatientResponse response) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(response.getUserId());
        userDTO.setEmail(response.getEmail());
        userDTO.setName(response.getName());
        userDTO.setGender(response.getGender().toString());
        userDTO.setPatientId(response.getPatientId());
        userDTO.setAddress(response.getAddress());
        userDTO.setDateOfBirth(response.getDateOfBirth());
        userDTO.setBloodGroup(response.getBloodGroup().toString());
        userDTO.setEmergencyContactName(response.getEmergencyContactName());
        userDTO.setEmergencyContactNumber(response.getEmergencyContactNumber());
        userDTO.setPhoneNumber(response.getPhoneNumber());

        return userDTO;
    }
}
