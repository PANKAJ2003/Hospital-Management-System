package com.pms.authservice.grpc;

import com.pms.authservice.dto.SignUpRequestDTO;
import com.pms.authservice.mapper.AuthPatientResponseMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import patient.AuthPatientResponse;
import patient.CreatePatientRequest;
import patient.PatientByUserIdRequest;
import patient.PatientIdResponse;
import patient.PatientServiceForAuthGrpc;

@Service
public class PatientAuthServiceGrpcClient {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(PatientAuthServiceGrpcClient.class);
    private final PatientServiceForAuthGrpc.PatientServiceForAuthBlockingStub blockingStub;

    public PatientAuthServiceGrpcClient(
            @Value("${patient.service.address}") String address,
            @Value("${patient.service.grpc.port}") int port
    ) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(address, port)
                .usePlaintext()
                .build();
        this.blockingStub = PatientServiceForAuthGrpc.newBlockingStub(channel);
    }

    public AuthPatientResponse createPatient(SignUpRequestDTO request, String userId) {
        try {
            if (request == null) {
                log.error("Request cannot be null in patientAuthServiceGrpcClient: createPatient()");
                throw new IllegalArgumentException("Request cannot be null");
            }
            CreatePatientRequest patientRequest = AuthPatientResponseMapper.toPatientRequestWithUserId(request, userId);

            AuthPatientResponse response = blockingStub.createPatient(patientRequest);
            if (!response.getSuccess()) {
                throw new RuntimeException("Patient creation failed");
            }
            log.info("Patient created successfully with ID: {}", response.getPatientId());
            return response;
        } catch (Exception e) {
            log.error("Error in patientAuthServiceGrpcClient: createPatient()", e);
            throw new RuntimeException("Failed to create patient: " + e.getMessage(), e);
        }
    }

    public AuthPatientResponse getPatientByUserId(String userId) {
        try {
            if (userId.isEmpty()) {
                log.error("UserId cannot be empty in patientAuthServiceGrpcClient: getPatientByUserId()");
                throw new IllegalArgumentException("UserId cannot be empty");
            }

            PatientByUserIdRequest request = PatientByUserIdRequest.newBuilder()
                    .setUserId(userId)
                    .build();
            log.info("Received request for finding patient by userId: {}", userId);
            AuthPatientResponse response = blockingStub.getPatientByUserId(request);
            if (!response.getSuccess()) {
                throw new RuntimeException("Patient not found for userId: " + userId);
            }
            log.info("Patient found successfully with ID: {}", response.getPatientId());
            return response;
        } catch (Exception e) {
            log.error("Error in patientAuthServiceGrpcClient: getPatientByUserId()", e);
            throw new RuntimeException("Failed to get patient by userId: " + e.getMessage(), e);
        }
    }

    public String getPatientIdByUserId(String userId) {
        try {
            if (userId.isEmpty()) {
                log.error("UserId cannot be empty in patientAuthServiceGrpcClient: getPatientIdByUserId()");
                throw new IllegalArgumentException("UserId cannot be empty");
            }
            PatientByUserIdRequest request = PatientByUserIdRequest.newBuilder()
                    .setUserId(userId).build();
            PatientIdResponse response = blockingStub.getPatientIdByUserId(request);

            if (!response.getSuccess()) {
                throw new RuntimeException("Patient not found for userId: " + userId);
            }
            log.info("Patient found successfully with ID: {}", response.getPatientId());
            return response.getPatientId();
        }
        catch (Exception e) {
            log.error("Error in patientAuthServiceGrpcClient: getPatientIdByUserId()", e);
            throw new RuntimeException("Failed to get patient by userId: " + e.getMessage(), e);
        }
    }
}
