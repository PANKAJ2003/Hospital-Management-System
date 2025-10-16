package com.pms.appointmentservice.controller;

import com.pms.appointmentservice.dto.DoctorTimeSlotsDTO;
import com.pms.appointmentservice.service.AppointmentAvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/appointment-slots")
public class AppointmentSlotsController {
    private final AppointmentAvailabilityService appointmentAvailabilityService;
    public AppointmentSlotsController(AppointmentAvailabilityService appointmentAvailabilityService) {
        this.appointmentAvailabilityService = appointmentAvailabilityService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorTimeSlotsDTO>> searchDoctors(
            @RequestParam(required = false) String speciality,
            @RequestParam(required = false) String name
    ){
        return ResponseEntity.ok(appointmentAvailabilityService.searchDoctors(speciality, name));
    }

}
