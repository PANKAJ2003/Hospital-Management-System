package com.pms.appointmentservice.service;

import com.pms.appointmentservice.client.DoctorServiceClient;
import com.pms.appointmentservice.dto.DoctorTimeSlotsDTO;
import com.pms.appointmentservice.dto.TimeSlotResponseDTO;
import com.pms.appointmentservice.dto.doctor.DoctorDTO;
import com.pms.appointmentservice.model.TimeSlot;
import com.pms.appointmentservice.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentAvailabilityService {
    private final DoctorServiceClient doctorServiceClient;
    private final TimeSlotRepository timeSlotRepository;

    public AppointmentAvailabilityService(DoctorServiceClient doctorServiceClient, TimeSlotRepository timeSlotRepository) {
        this.doctorServiceClient = doctorServiceClient;
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<DoctorTimeSlotsDTO> searchDoctors(String speciality, String name) {
        List<DoctorDTO> doctorsList = doctorServiceClient.searchDoctors(speciality, name);

        if (doctorsList.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch time slots available for each doctor after today
        List<UUID> doctorIds = doctorsList.stream()
                .map(DoctorDTO::getDoctorId)
                .toList();

        List<TimeSlot> availableSlots = timeSlotRepository.findAllByDoctorIdInAndDateAfter(doctorIds, LocalDate.now());

        Map<UUID, List<TimeSlotResponseDTO>> doctorIdToSlots = availableSlots.stream()
                .collect(Collectors.groupingBy(
                        TimeSlot::getDoctorId,
                        Collectors.mapping(ts -> new TimeSlotResponseDTO(
                                        ts.getSlotId(), ts.getDate(), ts.getStartTime(), ts.getEndTime(), ts.getStatus()),
                                Collectors.toList())
                ));

        // Step 4: Build DoctorTimeSlotsDTO list
        return doctorsList.stream()
                .map(doctor -> {
                    DoctorTimeSlotsDTO dto = new DoctorTimeSlotsDTO();
                    dto.setDoctorId(String.valueOf(doctor.getDoctorId()));
                    dto.setDoctorName(doctor.getName());
                    dto.setDoctorSpecialty(doctor.getSpecialty());
                    dto.setDoctorAvatar(doctor.getAvatar());
                    dto.setDoctorPricePerSession(doctor.getPricePerSession());
                    dto.setTimeSlots(doctorIdToSlots.getOrDefault(doctor.getDoctorId(), Collections.emptyList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
