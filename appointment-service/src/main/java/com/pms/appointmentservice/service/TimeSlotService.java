package com.pms.appointmentservice.service;

import com.pms.appointmentservice.dto.TimeSlotResponseDTO;
import com.pms.appointmentservice.dto.TimeSlotRequestDTO;
import com.pms.appointmentservice.exception.TimeSlotNotFoundException;
import com.pms.appointmentservice.mapper.TimeSlotMapper;
import com.pms.appointmentservice.model.TimeSlot;
import com.pms.appointmentservice.repository.TimeSlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public TimeSlotResponseDTO createTimeSlot(TimeSlotRequestDTO request) {
        log.info("Creating time slot: {}", request);
        TimeSlot timeSlot = TimeSlotMapper.toModel(request);
        TimeSlot saved = timeSlotRepository.save(timeSlot);
        log.info("Time slot created successfully with id: {}", saved.getSlotId());
        return TimeSlotMapper.toDTO(saved);
    }

    public TimeSlotResponseDTO getTimeSlotById(UUID id) {
        log.info("Fetching time slot with id: {}", id);
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException("Time slot not found with id: " + id));
        return TimeSlotMapper.toDTO(timeSlot);
    }

    public List<TimeSlotResponseDTO> getAllTimeSlots() {
        log.info("Fetching all time slots");
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();
        return timeSlots.stream()
                .map(TimeSlotMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TimeSlotResponseDTO updateTimeSlot(UUID id, TimeSlotRequestDTO request) {
        log.info("Updating time slot with id: {}", id);
        TimeSlot existingTimeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException("Time slot not found with id: " + id));

        // Update the existing time slot with new values
        TimeSlot updatedTimeSlot = TimeSlotMapper.toModel(request);
        updatedTimeSlot.setSlotId(existingTimeSlot.getSlotId());

        TimeSlot saved = timeSlotRepository.save(updatedTimeSlot);
        log.info("Time slot updated successfully with id: {}", id);
        return TimeSlotMapper.toDTO(saved);
    }

    public void deleteTimeSlot(UUID id) {
        log.info("Deleting time slot with id: {}", id);
        if (!timeSlotRepository.existsById(id)) {
            throw new TimeSlotNotFoundException("Time slot not found with id: " + id);
        }
        timeSlotRepository.deleteById(id);
        log.info("Time slot deleted successfully with id: {}", id);
    }

}