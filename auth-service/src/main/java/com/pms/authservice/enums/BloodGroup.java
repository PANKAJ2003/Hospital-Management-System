package com.pms.authservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BloodGroup {
    A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE;

    @JsonCreator
    public static BloodGroup fromString(String value) {
        if (value == null) {
            return null;
        }
        // Replace "-" with "_" and make uppercase to match enum constants
        return BloodGroup.valueOf(value.replace("-", "_").toUpperCase());
    }
}
