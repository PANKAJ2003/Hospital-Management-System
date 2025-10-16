package com.pms.appointmentservice.controller;

import com.pms.appointmentservice.dto.TimeSlotResponseDTO;
import com.pms.appointmentservice.dto.TimeSlotRequestDTO;
import com.pms.appointmentservice.service.TimeSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/timeslots")
public class TimeSlotController {
    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping
    public ResponseEntity<TimeSlotResponseDTO> createTimeSlot(@Valid @RequestBody TimeSlotRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(timeSlotService.createTimeSlot(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotResponseDTO> getTimeSlotById(@PathVariable UUID id){
        return ResponseEntity.ok(timeSlotService.getTimeSlotById(id));
    }

    @GetMapping
    public ResponseEntity<List<TimeSlotResponseDTO>> getAllTimeSlots(){
        return ResponseEntity.ok(timeSlotService.getAllTimeSlots());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeSlotResponseDTO> updateTimeSlot(
            @PathVariable UUID id,
            @Valid @RequestBody TimeSlotRequestDTO request){
        return ResponseEntity.ok(timeSlotService.updateTimeSlot(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable UUID id){
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }
}