package com.pms.appointmentservice.client;

import com.pms.appointmentservice.dto.doctor.DoctorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "doctor-service", url = "${doctor.service.url}")
public interface DoctorServiceClient {

    @GetMapping("/doctors/{doctorId}")
    DoctorDTO getDoctorById(@PathVariable("doctorId") UUID doctorId);

    @PostMapping("/doctors/batch")
    List<DoctorDTO> getDoctorsByIds(@RequestBody List<UUID> doctorIds);

    @GetMapping("/doctors/search")
    List<DoctorDTO> searchDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String name
    );
}