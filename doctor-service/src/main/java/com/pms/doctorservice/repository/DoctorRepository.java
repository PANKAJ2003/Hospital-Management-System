package com.pms.doctorservice.repository;

import com.pms.doctorservice.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    // One query to rule them all!
    @Query("SELECT d FROM Doctor d WHERE " +
            "(:specialty IS NULL OR :specialty = '' OR LOWER(d.specialty) LIKE LOWER(CONCAT('%', :specialty, '%'))) AND " +
            "(:name IS NULL OR :name = '' OR " +
            "LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Doctor> searchDoctors(@Param("specialty") String specialty, @Param("name") String name);

    @Query("SELECT d FROM Doctor d WHERE d.doctorId IN :ids")
    List<Doctor> findByDoctorIdIn(@Param("ids") List<UUID> ids);
}