package com.hirehub.common.enums;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public enum RecruiterVerificationStatus {
    PENDING_AUTO_CHECK,
    APPROVED,
    REVIEW_REQUIRED,
    REJECTED
}
