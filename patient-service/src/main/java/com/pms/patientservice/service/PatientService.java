package com.pms.patientservice.service;

import com.pms.patientservice.dto.PatientRequestDTO;
import com.pms.patientservice.dto.PatientResponseDTO;
import com.pms.patientservice.exception.EmailAlreadyExistsException;
import com.pms.patientservice.exception.PatientNotFoundException;
import com.pms.patientservice.grpc.BillingServiceGrpcClient;
import com.pms.patientservice.kafka.KafkaProducer;
import com.pms.patientservice.mapper.PatientMapper;
import com.pms.patientservice.model.Patient;
import com.pms.patientservice.repository.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists" + patientRequestDTO.getEmail());
        }
        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        billingServiceGrpcClient.createBillingAccount(
                newPatient.getId().toString(),
                newPatient.getName(),
                newPatient.getEmail());

        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID patientId, PatientRequestDTO patientRequestDTO) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            throw new PatientNotFoundException("Patient not found with ID:" + patientId);
        }

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), patientId)) {
            throw new EmailAlreadyExistsException("A patient with this email already exists" + patientRequestDTO.getEmail());
        }

        patient.get().setName(patientRequestDTO.getName());
        patient.get().setAddress(patientRequestDTO.getAddress());
        patient.get().setGender(patientRequestDTO.getGender());
        patient.get().setDateOfBirth(patientRequestDTO.getDateOfBirth());
        patient.get().setEmail(patientRequestDTO.getEmail());

        Patient updatedPatient = patientRepository.save(patient.get());
        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID patientId) {
        patientRepository.deleteById(patientId);
    }

    public PatientResponseDTO getPatient(UUID patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            throw new PatientNotFoundException("Patient not found with ID:" + patientId);
        }
        return PatientMapper.toDTO(patient.get());
    }

    public boolean patientExistsById(UUID patientId) {
        return patientRepository.existsById(patientId);
    }

    public PatientResponseDTO getPatientByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        Patient patient = patientRepository.findByUserId(userId);
        if (patient == null) {
            throw new PatientNotFoundException("Patient not found for userId: " + userId);
        }
        return PatientMapper.toDTO(patient);
    }
}
