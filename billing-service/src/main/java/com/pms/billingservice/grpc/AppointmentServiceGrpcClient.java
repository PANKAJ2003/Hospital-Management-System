package com.pms.billingservice.grpc;

import appointment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppointmentServiceGrpcClient {
    private final AppointmentServiceGrpc.AppointmentServiceBlockingStub appointmentServiceBlockingStub;

    public AppointmentServiceGrpcClient(
            @Value("${appointment.service.address}") String serverAddress,
            @Value("${appointment.service.grpc.port}") int serverPort
    ) {
        log.info("Connecting to Appointment Service GRPC service at {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();
        appointmentServiceBlockingStub = AppointmentServiceGrpc.newBlockingStub(channel);
    }

    public AppointmentResponse getAppointment(String id) {
        try {
            AppointmentRequest request = AppointmentRequest.newBuilder()
                    .setAppointmentId(id).build();
            return appointmentServiceBlockingStub.getAppointment(request);
        } catch (Exception e) {
            log.error("Error while fetching appointment: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public UpdateAppointmentStatusResponse updateAppointmentStatus(String appointmentId,
                                                                   String transactionId,
                                                                   boolean status,
                                                                   String paymentMethod
    ) {
        try {
            UpdateAppointmentStatusRequest request = UpdateAppointmentStatusRequest.newBuilder()
                    .setAppointmentId(appointmentId)
                    .setTransactionId(transactionId)
                    .setPaymentSuccess(status)
                    .setPaymentMethod(paymentMethod)
                    .build();
            return appointmentServiceBlockingStub.updateAppointmentStatus(request);
        } catch (Exception e) {
            log.error("Error while updating appointment status: {}", e.getMessage());
            return null;
        }
    }

}
