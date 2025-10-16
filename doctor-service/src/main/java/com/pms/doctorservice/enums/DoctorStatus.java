package com.pms.doctorservice.enums;

public enum DoctorStatus {
    ACTIVE,           // Currently practicing
    INACTIVE,         // Temporarily not available
    ON_LEAVE,         // On medical/personal leave
    SUSPENDED,        // Account suspended
    RETIRED,          // No longer practicing
    PENDING_APPROVAL  // Awaiting admin approval
}