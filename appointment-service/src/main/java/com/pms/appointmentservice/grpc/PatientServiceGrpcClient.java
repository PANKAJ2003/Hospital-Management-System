package com.pms.appointmentservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import patient.PatientRequest;
import patient.PatientResponse;
import patient.PatientServiceGrpc;

@Service
public class PatientServiceGrpcClient {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(PatientServiceGrpcClient.class);
    private final PatientServiceGrpc.PatientServiceBlockingStub blockingStub;

    public PatientServiceGrpcClient(
            @Value("${patient.service.address:localhost}") String serverAddress,
            @Value("${patient.service.grpc.port:9002}") int serverPort
    ) {
        log.info("Connecting to Patient Service GRPC service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        blockingStub = PatientServiceGrpc.newBlockingStub(channel);
    }

    public patient.PatientResponse getPatientById(String patientId) {
        PatientRequest patientRequest = PatientRequest.newBuilder()
                .setPatientId(patientId)
                .build();

        PatientResponse patientResponse = blockingStub.getPatientById(patientRequest);
        log.info("Received response from Patient Service: {}", patientResponse.toString());
        return patientResponse;
    }

    public boolean doesPatientExists(String patientId) {
        PatientRequest patientRequest = PatientRequest.newBuilder()
                .setPatientId(patientId)
                .build();
        return blockingStub.doesPatientExists(patientRequest).getPatientExists();
    }
}
