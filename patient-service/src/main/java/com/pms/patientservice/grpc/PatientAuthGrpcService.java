package com.pms.patientservice.grpc;

import com.pms.patientservice.dto.PatientRequestDTO;
import com.pms.patientservice.dto.PatientResponseDTO;
import com.pms.patientservice.enums.BloodGroup;
import com.pms.patientservice.enums.Gender;
import com.pms.patientservice.exception.PatientNotFoundException;
import com.pms.patientservice.service.PatientService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.*;

import java.time.LocalDate;
import java.util.UUID;

@GrpcService
public class PatientAuthGrpcService extends PatientServiceForAuthGrpc.PatientServiceForAuthImplBase {
    private final Logger log = LoggerFactory.getLogger(PatientAuthGrpcService.class);
    private final PatientService patientService;

    public PatientAuthGrpcService(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public void createPatient(CreatePatientRequest request, StreamObserver<AuthPatientResponse> responseObserver) {
        try {
            if (request == null) {
                log.error("Request cannot be null in patientAuthGrpcService: createPatient()");
                throw new IllegalArgumentException("Request cannot be null");
            }

            PatientRequestDTO patientRequest = new PatientRequestDTO();
            patientRequest.setName(request.getName());
            patientRequest.setEmail(request.getEmail());
            patientRequest.setAddress(request.getAddress());
            patientRequest.setPhoneNumber(request.getPhoneNumber());
            patientRequest.setGender(Gender.valueOf(request.getGender().toString()));
            patientRequest.setEmergencyContactName(request.getEmergencyContactName());
            patientRequest.setEmergencyContactNumber(request.getEmergencyContactNumber());
            patientRequest.setBloodGroup(BloodGroup.valueOf(request.getBloodGroup().toString()));
            patientRequest.setUserId(UUID.fromString(request.getUserId()));
            patientRequest.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            patientRequest.setRegisteredDate(LocalDate.now());

            PatientResponseDTO createdPatient = patientService.createPatient(patientRequest);

            AuthPatientResponse authPatientResponse = buildAuthPatientResponse(createdPatient);

            log.info("Patient created successfully with ID: {}", createdPatient.getId());

            responseObserver.onNext(authPatientResponse);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument in createPatient: {}", e.getMessage());

            AuthPatientResponse errorResponse = AuthPatientResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Invalid input: " + e.getMessage())
                    .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error creating patient: {}", e.getMessage(), e);

            AuthPatientResponse errorResponse = AuthPatientResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to create patient: " + e.getMessage())
                    .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getPatientByUserId(patient.PatientByUserIdRequest request, StreamObserver<AuthPatientResponse> responseObserver) {
        try {
            if (request == null || request.getUserId().isEmpty()) {
                log.error("Request cannot be null in patientAuthGrpcService: getPatientByUserId()");
                throw new IllegalArgumentException("Request cannot be null");
            }
            log.info("Received request for finding patient by userId: {}", request.getUserId());
            PatientResponseDTO patientByUserId = patientService.getPatientByUserId(UUID.fromString(request.getUserId()));
            if (patientByUserId == null) {
                log.error("Patient not found for userId: {}", request.getUserId());
                throw new PatientNotFoundException("Patient not found for userId: " + request.getUserId());
            }
            AuthPatientResponse authPatientResponse = buildAuthPatientResponse(patientByUserId);
            responseObserver.onNext(authPatientResponse);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument in getPatientByUserId: {}", e.getMessage());
            AuthPatientResponse response = AuthPatientResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Invalid input: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
        } catch (Exception ex) {
            assert request != null;
            log.error("Failed to get patient by userId: {}", request.getUserId(), ex);
            AuthPatientResponse response = AuthPatientResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to get patient by userId: " + request.getUserId())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    @Override
    public void getPatientIdByUserId(PatientByUserIdRequest request, StreamObserver<PatientIdResponse> responseObserver) {
        if (request == null || request.getUserId().isEmpty()) {
            responseObserver.onNext(PatientIdResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Request cannot be null or empty")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        try {
            UUID userId = UUID.fromString(request.getUserId());
            PatientResponseDTO patient = patientService.getPatientByUserId(userId);

            if (patient == null) {
                responseObserver.onNext(PatientIdResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Patient not found for userId: " + request.getUserId())
                        .build());
            } else {
                responseObserver.onNext(PatientIdResponse.newBuilder()
                        .setPatientId(patient.getId())
                        .setSuccess(true)
                        .build());
            }
        } catch (Exception ex) {
            log.error("Failed to get patient id for userId: {}", request.getUserId(), ex);
            responseObserver.onNext(PatientIdResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Internal error occurred")
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }



    private AuthPatientResponse buildAuthPatientResponse(PatientResponseDTO patientResponse) {

        return AuthPatientResponse.newBuilder()
                .setPatientId(patientResponse.getId())
                .setUserId(patientResponse.getUserId())
                .setName(patientResponse.getName())
                .setEmail(patientResponse.getEmail())
                .setAddress(patientResponse.getAddress())
                .setPhoneNumber(patientResponse.getPhoneNumber())
                .setDateOfBirth(patientResponse.getDateOfBirth())
                .setGender(patient.Gender.valueOf(patientResponse.getGender()))
                .setBloodGroup(patient.BloodGroup.valueOf(patientResponse.getBloodGroup()))
                .setEmergencyContactName(patientResponse.getEmergencyContactName())
                .setEmergencyContactNumber(patientResponse.getEmergencyContactNumber())
                .setSuccess(true)
                .setMessage("Patient created successfully")
                .build();
    }
}
