package com.pms.appointmentservice.mapper;

import com.pms.appointmentservice.dto.TimeSlotResponseDTO;
import com.pms.appointmentservice.dto.TimeSlotRequestDTO;
import com.pms.appointmentservice.model.TimeSlot;

public class TimeSlotMapper {
    public static TimeSlot toModel(TimeSlotRequestDTO dto){
        TimeSlot model = new TimeSlot();
        model.setStartTime(dto.getStartTime());
        model.setEndTime(dto.getEndTime());
        model.setDoctorId(dto.getDoctorId());
        model.setDate(dto.getDate());
        return model;
    }

    public static TimeSlotResponseDTO toDTO(TimeSlot model){
        TimeSlotResponseDTO dto = new TimeSlotResponseDTO();
        dto.setId(model.getSlotId());
        dto.setStartTime(model.getStartTime());
        dto.setEndTime(model.getEndTime());
        dto.setDoctorId(model.getDoctorId());
        dto.setDate(model.getDate());
        dto.setStatus(model.getStatus());
        dto.setDoctorId(model.getDoctorId());
        return dto;
    }
}
