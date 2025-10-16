package com.pms.doctorservice.controller;

import com.pms.doctorservice.dto.DoctorDTO;
import com.pms.doctorservice.dto.DoctorRequestDTO;
import com.pms.doctorservice.model.Doctor;
import com.pms.doctorservice.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @RequestBody DoctorRequestDTO dto) {
        return ResponseEntity.ok(doctorService.createDoctor(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorDTO> updateDoctor(@PathVariable UUID id,@Valid @RequestBody DoctorRequestDTO dto) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    // Get multiple doctors - IMPORTANT for appointment service
    @PostMapping("/batch")
    public ResponseEntity<List<DoctorDTO>> getDoctorsByIds(@RequestBody List<UUID> doctorIds) {
        return ResponseEntity.ok(doctorService.getDoctorsByIds(doctorIds));
    }

    // Search doctors by specialty
    @GetMapping("/search")
    public ResponseEntity<List<DoctorDTO>> searchDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(doctorService.searchDoctors(specialty, name));
    }
}
