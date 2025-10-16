package com.pms.doctorservice.service;

import com.pms.doctorservice.dto.DoctorDTO;
import com.pms.doctorservice.dto.DoctorRequestDTO;
import com.pms.doctorservice.mapper.DoctorMapper;
import com.pms.doctorservice.model.Doctor;
import com.pms.doctorservice.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorDTO createDoctor(DoctorRequestDTO dto) {
        Doctor doctor = DoctorMapper.toModel(dto);
        return DoctorMapper.toDto(doctorRepository.save(doctor));
    }

    public DoctorDTO updateDoctor(UUID id, DoctorRequestDTO dto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        DoctorMapper.updateModel(doctor, dto);
        return DoctorMapper.toDto(doctorRepository.save(doctor));
    }

    public DoctorDTO getDoctorById(UUID id) {
        return DoctorMapper.toDto(
                doctorRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Doctor not found"))
        );
    }

    public List<DoctorDTO> getAllDoctors() {
        return DoctorMapper.toDtoList(doctorRepository.findAll());
    }

    public void deleteDoctor(UUID id) {
        doctorRepository.deleteById(id);
    }

    public List<DoctorDTO> getDoctorsByIds(List<UUID> doctorIds) {
        List<Doctor> doctors = doctorRepository.findAllById(doctorIds);
        return doctors.stream()
                .map(DoctorMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> searchDoctors(String specialty, String name) {
        String specialtyParam = (specialty != null && !specialty.isBlank()) ? specialty : null;
        String nameParam = (name != null && !name.isBlank()) ? name : null;

        List<Doctor> doctors = doctorRepository.searchDoctors(specialtyParam, nameParam);

        return doctors.stream()
                .map(DoctorMapper::toDto)
                .collect(Collectors.toList());
    }

}
