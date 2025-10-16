package com.pms.patientservice.grpc;

import com.pms.patientservice.dto.PatientResponseDTO;
import com.pms.patientservice.service.PatientService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.PatientExistsResponse;
import patient.PatientRequest;
import patient.PatientResponse;
import patient.PatientServiceGrpc;

import java.util.UUID;

@GrpcService
public class PatientGrpcService extends PatientServiceGrpc.PatientServiceImplBase{
    private final static Logger log = LoggerFactory.getLogger(PatientGrpcService.class);
    private final PatientService patientService;

    public PatientGrpcService(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public void doesPatientExists(PatientRequest request, StreamObserver<PatientExistsResponse> responseObserver) {
        if (request.getPatientId().isEmpty()) {
            log.error("Patient ID cannot be null in patientGrpcService: doesPatientExists()");
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        boolean patientExists = patientService.patientExistsById((UUID.fromString(request.getPatientId())));

        PatientExistsResponse response = PatientExistsResponse.newBuilder()
                .setPatientExists(patientExists)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPatientById(PatientRequest request, StreamObserver<patient.PatientResponse> responseObserver) {
        if (request.getPatientId().isEmpty()) {
            log.error("Patient ID cannot be null in patientGrpcService : getPatientById()");
            throw new IllegalArgumentException("Patient ID cannot be null");
        }

        PatientResponseDTO patientResponseDTO = patientService.getPatient(UUID.fromString(request.getPatientId()));

        PatientResponse patientResponse = PatientResponse.newBuilder()
                .setPatientId(patientResponseDTO.getId())
                .setName(patientResponseDTO.getName())
                .setEmail(patientResponseDTO.getEmail())
                .setDateOfBirth(patientResponseDTO.getDateOfBirth())
                .setGender(patientResponseDTO.getGender())
                .build();
        responseObserver.onNext(patientResponse);
        responseObserver.onCompleted();
    }

}
