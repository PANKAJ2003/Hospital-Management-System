package com.pms.appointmentservice.grpc;

import appointment.AppointmentRequest;
import appointment.AppointmentResponse;
import appointment.UpdateAppointmentStatusRequest;
import appointment.UpdateAppointmentStatusResponse;
import com.pms.appointmentservice.dto.AppointmentResponseDTO;
import com.pms.appointmentservice.service.AppointmentService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@Slf4j
@GrpcService
public class AppointmentServiceGrpc extends appointment.AppointmentServiceGrpc.AppointmentServiceImplBase {
    private final AppointmentService appointmentService;

    public AppointmentServiceGrpc(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    public void getAppointment(AppointmentRequest request, StreamObserver<AppointmentResponse> responseObserver) {
        if (request.getAppointmentId().isEmpty()) {
            log.error("Appointment ID cannot be null in appointmentServiceGrpc: getAppointment()");
            throw new IllegalArgumentException("Appointment ID cannot be null");
        }
        try {
            AppointmentResponseDTO appointment = appointmentService.getAppointment(UUID.fromString(request.getAppointmentId()));
            AppointmentResponse response = AppointmentResponse.newBuilder()
                    .setAppointmentId(appointment.getAppointmentId().toString())
                    .setStatus(appointment.getStatus().name())
                    .setPaymentStatus(appointment.getPaymentStatus().name())
                    .setAmount(appointment.getAmount().longValue())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while fetching appointment: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Unable to fetch appointment")
                    .withCause(e)
                    .asRuntimeException());
        }

    }

    @Override
    public void updateAppointmentStatus(UpdateAppointmentStatusRequest request, StreamObserver<appointment.UpdateAppointmentStatusResponse> responseObserver) {
        if (request.getAppointmentId().isEmpty()) {
            log.error("Appointment ID cannot be null in appointmentServiceGrpc: UpdateAppointmentStatus()");
            throw new IllegalArgumentException("Appointment ID cannot be null");
        }

        try {
            AppointmentResponseDTO updatedAppointment = appointmentService.updateAppointmentStatus(
                    request.getAppointmentId(),
                    request.getTransactionId(),
                    request.getPaymentSuccess(),
                    request.getPaymentMethod()
            );
            UpdateAppointmentStatusResponse response = UpdateAppointmentStatusResponse.newBuilder()
                    .setAppointmentId(updatedAppointment.getAppointmentId().toString())
                    .setStatus(updatedAppointment.getStatus().name())
                    .setPaymentStatus(updatedAppointment.getPaymentStatus().name())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while updating appointment status: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Unable to update appointment status")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
