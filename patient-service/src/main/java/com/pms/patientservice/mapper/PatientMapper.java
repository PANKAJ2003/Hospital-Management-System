package com.pms.patientservice.mapper;

import com.pms.patientservice.dto.PatientRequestDTO;
import com.pms.patientservice.dto.PatientResponseDTO;
import com.pms.patientservice.model.Patient;

public class PatientMapper {
    public static PatientResponseDTO toDTO(Patient patient) {
        PatientResponseDTO patientDTO = new PatientResponseDTO();
        patientDTO.setId(patient.getId().toString());
        patientDTO.setName(patient.getName());
        patientDTO.setEmail(patient.getEmail());
        patientDTO.setAddress(patient.getAddress());
        patientDTO.setDateOfBirth(patient.getDateOfBirth().toString());
        patientDTO.setGender(patient.getGender().toString());
        patientDTO.setUserId(patient.getUserId().toString());
        patientDTO.setBloodGroup(patient.getBloodGroup().toString());
        patientDTO.setEmergencyContactNumber(patient.getEmergencyContactNumber());
        patientDTO.setEmergencyContactName(patient.getEmergencyContactName());
        patientDTO.setPhoneNumber(patient.getPhoneNumber());
        return patientDTO;
    }

    public static Patient toModel(PatientRequestDTO patientRequestDTO) {
        Patient patient = new Patient();
        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setGender(patientRequestDTO.getGender());
        patient.setDateOfBirth(patientRequestDTO.getDateOfBirth());
        patient.setRegisteredDate(patientRequestDTO.getRegisteredDate());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setUserId(patientRequestDTO.getUserId());
        patient.setBloodGroup(patientRequestDTO.getBloodGroup());
        patient.setEmergencyContactNumber(patientRequestDTO.getEmergencyContactNumber());
        patient.setEmergencyContactName(patientRequestDTO.getEmergencyContactName());
        patient.setPhoneNumber(patientRequestDTO.getPhoneNumber());
        return patient;
    }

}
