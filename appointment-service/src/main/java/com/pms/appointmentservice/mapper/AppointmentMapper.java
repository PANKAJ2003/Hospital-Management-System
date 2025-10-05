package com.pms.appointmentservice.mapper;

import com.pms.appointmentservice.dto.AppointmentRequestDTO;
import com.pms.appointmentservice.dto.AppointmentResponseDTO;
import com.pms.appointmentservice.model.Appointment;

import java.time.LocalDateTime;

public class AppointmentMapper {
    public static Appointment toModel(AppointmentRequestDTO request) {
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        LocalDateTime dateTime = LocalDateTime.of(request.getDate(), request.getTime());
        appointment.setAppointmentDateTime(dateTime);

        if (request.getReasonForVisit() != null) {
            appointment.setReasonForVisit(request.getReasonForVisit());
        }

        if (request.getVisitType() != null) {
            appointment.setType(request.getVisitType());
        }
        appointment.setAmount(request.getAmount());

        return appointment;
    }

    public static AppointmentResponseDTO toDTO(Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getAppointmentId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getAppointmentDateTime(),
                appointment.getStatus(),
                appointment.getType(),
                appointment.getReasonForVisit(),
                appointment.getCreatedAt(),
                appointment.getAmount()
        );
    }

}
