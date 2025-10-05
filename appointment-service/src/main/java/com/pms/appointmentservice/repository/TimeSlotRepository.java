package com.pms.appointmentservice.repository;

import com.pms.appointmentservice.model.TimeSlot;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    @Modifying
    @Transactional
    @Query("UPDATE TimeSlot t SET t.status = 'UNAVAILABLE' " +
            "WHERE t.doctorId = :doctorId AND t.date = :date AND t.startTime = :startTime AND t.status = 'AVAILABLE'")
    int bookSlot(@Param("doctorId") UUID doctorId,
                 @Param("date") LocalDate date,
                 @Param("startTime") LocalTime startTime);

    @Query("SELECT t FROM TimeSlot t WHERE t.doctorId = :doctorId AND t.date = :date AND t.startTime = :startTime")
    Optional<TimeSlot> findByDoctorIdAndDateAndStartTime(@Param("doctorId") UUID doctorId
    , @Param("date") LocalDate date, @Param("startTime") LocalTime startTime);
}
