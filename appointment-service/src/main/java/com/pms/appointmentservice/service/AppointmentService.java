package com.pms.appointmentservice.service;

import com.pms.appointmentservice.dto.AppointmentRequestDTO;
import com.pms.appointmentservice.dto.AppointmentResponseDTO;
import com.pms.appointmentservice.enums.AppointmentStatus;
import com.pms.appointmentservice.enums.TimeSlotStatus;
import com.pms.appointmentservice.exception.AppointmentDoesNotExistException;
import com.pms.appointmentservice.exception.NoTimeSlotAvaliableException;
import com.pms.appointmentservice.exception.PatientNotFoundException;
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
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private final PatientServiceGrpcClient patientServiceGrpc;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TimeSlotRepository timeSlotRepository,
                              PatientServiceGrpcClient patientServiceGrpc) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.patientServiceGrpc = patientServiceGrpc;
    }

    @Transactional
    public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO request) {
        boolean doesPatientExists = patientServiceGrpc.doesPatientExists(request.getPatientId().toString());
        if (!doesPatientExists) {
            log.error("Patient does not exist with id: {}", request.getPatientId());
            throw new PatientNotFoundException("Patient does not exist with id: " + request.getPatientId());
        }
        int booked = timeSlotRepository.bookSlot(request.getDoctorId(), request.getDate(), request.getTime());
        if (booked == 0) {
            log.error("No time slots available for doctor id: {} on date: {} at time: {}",
                    request.getDoctorId(), request.getDate(), request.getTime());
            throw new NoTimeSlotAvaliableException("No time slots available for doctor id: "
                    + request.getDoctorId() + " on date: " + request.getDate() + " at time: " + request.getTime());
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

        LocalDateTime appointmentTime = appointment.getAppointmentDateTime();

        // Only allow cancel if it's in the future
        if (appointmentTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel past appointments");
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
