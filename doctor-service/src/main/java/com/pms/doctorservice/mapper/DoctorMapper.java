package com.pms.doctorservice.mapper;

import com.pms.doctorservice.dto.DoctorDTO;
import com.pms.doctorservice.dto.DoctorRequestDTO;
import com.pms.doctorservice.enums.DoctorStatus;
import com.pms.doctorservice.model.Doctor;

import java.util.List;
import java.util.stream.Collectors;

public class DoctorMapper {
    public static Doctor toModel(DoctorRequestDTO dto) {
        if (dto == null) return null;

        Doctor doctor = new Doctor();
        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setEmail(dto.getEmail());
        doctor.setPhone(dto.getPhone());
        doctor.setGender(dto.getGender());
        doctor.setDateOfBirth(dto.getDateOfBirth());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setHospitalName(dto.getHospitalName());
        doctor.setBio(dto.getBio());
        doctor.setProfileImageUrl(dto.getProfileImageUrl());
        doctor.setStatus(dto.getStatus() != null ? dto.getStatus() : DoctorStatus.ACTIVE);
        doctor.setQualifications(dto.getQualifications());
        doctor.setExperienceMonths(dto.getExperienceMonths());

        return doctor;
    }

    public static void updateModel(Doctor doctor, DoctorRequestDTO dto) {
        if (doctor == null || dto == null) return;

        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setEmail(dto.getEmail());
        doctor.setPhone(dto.getPhone());
        doctor.setGender(dto.getGender());
        doctor.setDateOfBirth(dto.getDateOfBirth());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setHospitalName(dto.getHospitalName());
        doctor.setBio(dto.getBio());
        doctor.setProfileImageUrl(dto.getProfileImageUrl());
        doctor.setStatus(dto.getStatus() != null ? dto.getStatus() : DoctorStatus.ACTIVE);
    }

    public static DoctorDTO toDto(Doctor doctor) {
        if (doctor == null) return null;

        DoctorDTO dto = new DoctorDTO();
        dto.setDoctorId(doctor.getDoctorId());
        dto.setName(doctor.getFirstName() + " " + doctor.getLastName());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setPricePerSession(doctor.getConsultationFee());
        dto.setEmail(doctor.getEmail());
        dto.setPhone(doctor.getPhone());
        dto.setAvatar(doctor.getProfileImageUrl());
        dto.setQualifications(doctor.getQualifications());
        dto.setExperienceMonths(doctor.getExperienceMonths());

        return dto;
    }

    public static List<DoctorDTO> toDtoList(List<Doctor> doctors) {
        if (doctors == null) return List.of();
        return doctors.stream()
                .map(DoctorMapper::toDto)
                .collect(Collectors.toList());
    }
}
