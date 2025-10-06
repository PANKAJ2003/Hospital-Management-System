package com.pms.appointmentservice.service;

import billing.TransactionType;
import billing.UpdateAmountResponse;
import com.pms.appointmentservice.dto.AppointmentRequestDTO;
import com.pms.appointmentservice.dto.AppointmentResponseDTO;
import com.pms.appointmentservice.enums.AppointmentStatus;
import com.pms.appointmentservice.enums.TimeSlotStatus;
import com.pms.appointmentservice.exception.AppointmentDoesNotExistException;
import com.pms.appointmentservice.exception.FailedToCreateAppointmentException;
import com.pms.appointmentservice.exception.NoTimeSlotAvaliableException;
import com.pms.appointmentservice.exception.PatientNotFoundException;
import com.pms.appointmentservice.grpc.BillingServiceGrpcClient;
import com.pms.appointmentservice.grpc.PatientServiceGrpcClient;
import com.pms.appointmentservice.mapper.AppointmentMapper;
import com.pms.appointmentservice.model.Appointment;
import com.pms.appointmentservice.repository.AppointmentRepository;
import com.pms.appointmentservice.repository.TimeSlotRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {

    /* Constant Variables */
    public static final long CURRENCY_SCALE = 100;

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private final PatientServiceGrpcClient patientServiceGrpc;
    private final BillingServiceGrpcClient billingServiceGrpcClient;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TimeSlotRepository timeSlotRepository,
                              PatientServiceGrpcClient patientServiceGrpc, BillingServiceGrpcClient billingServiceGrpcClient
    ) {

        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.patientServiceGrpc = patientServiceGrpc;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
    }

    @Transactional
    public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO request) {
        boolean doesPatientExists = patientServiceGrpc.doesPatientExists(request.getPatientId().toString());
        if (!doesPatientExists) {
            log.error("Patient does not exist with id: {}", request.getPatientId());
            throw new PatientNotFoundException("Patient does not exist with id: " + request.getPatientId());
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(request.getDate(), request.getTime());
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            log.error("Cannot book past appointment");
            throw new FailedToCreateAppointmentException("Cannot book past appointment");
        }

        int booked = timeSlotRepository.bookSlot(request.getDoctorId(), request.getDate(), request.getTime());
        if (booked == 0) {
            log.error("No time slots available for doctor id: {} on date: {} at time: {}",
                    request.getDoctorId(), request.getDate(), request.getTime());
            throw new NoTimeSlotAvaliableException("No time slots available for doctor id: "
                    + request.getDoctorId() + " on date: " + request.getDate() + " at time: " + request.getTime());
        }

        String billingAccountId = billingServiceGrpcClient
                .findBillingAccountIdByPatientId(request.getPatientId().toString());

        if (billingAccountId == null) {
            throw new RuntimeException("Billing account not found for patient id: " + request.getPatientId());
        }

        Optional<UpdateAmountResponse> billingResponse = billingServiceGrpcClient.updateAmountToBillingAccount(
                billingAccountId,
                request.getAmount().longValue() * CURRENCY_SCALE,
                CURRENCY_SCALE,
                TransactionType.CHARGE
        );

        if (billingResponse.isEmpty()) {
            log.error("Failed to add amount to billing account");
            throw new FailedToCreateAppointmentException("Failed to create appointment");
        }
        Appointment appointment = AppointmentMapper.toModel(request);
        appointmentRepository.save(appointment);
        return AppointmentMapper.toDTO(appointment);
    }

    @Transactional
    public void cancelAppointment(UUID appointmentId) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("Appointment ID cannot be null");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentDoesNotExistException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentDoesNotExistException("Appointment already cancelled");
        }

        LocalDateTime appointmentTime = appointment.getAppointmentDateTime();

        // Only allow cancel if it's in the future
        if (appointmentTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel past appointments");
        }

        String billingAccountId = billingServiceGrpcClient
                .findBillingAccountIdByPatientId(appointment.getPatientId().toString());

        if (billingAccountId == null) {
            throw new RuntimeException("Billing account not found for patient id: " + appointment.getPatientId());
        }

        Optional<UpdateAmountResponse> billingResponse = billingServiceGrpcClient.updateAmountToBillingAccount(
                billingAccountId,
                appointment.getAmount().longValue() * CURRENCY_SCALE,
                CURRENCY_SCALE,
                TransactionType.REFUND
        );

        if (billingResponse.isEmpty()) {
            log.error("Failed to deduct amount from billing account");
            throw new RuntimeException("Failed to cancel appointment");
        }

        // Update appointment status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // Mark the time slot as available again
        timeSlotRepository.findByDoctorIdAndDateAndStartTime(
                appointment.getDoctorId(),
                appointmentTime.toLocalDate(),
                appointmentTime.toLocalTime()
        ).ifPresent(timeSlot -> {
            timeSlot.setStatus(TimeSlotStatus.AVAILABLE);
            timeSlotRepository.save(timeSlot);
        });
    }


    public AppointmentResponseDTO getAppointment(UUID appointmentId) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("Appointment ID cannot be null");
        }

        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        if (appointment.isEmpty()) {
            throw new AppointmentDoesNotExistException("Appointment not found with id: " + appointmentId);
        }

        return AppointmentMapper.toDTO(appointment.get());
    }

    public List<AppointmentResponseDTO> getAppointmentsByPatientId(UUID patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }

        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream().map(AppointmentMapper::toDTO).toList();
    }
}
