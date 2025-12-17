package com.payment.service.model.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum PaymentStatus {
    SUCCESS,
    FAILED,
}
