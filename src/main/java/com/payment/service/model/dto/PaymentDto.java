package com.payment.service.model.dto;

import com.payment.service.model.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentDto {

    private String id;

    @NotBlank
    private String orderId;

    @NotBlank
    private String userId;

    @NotBlank
    private PaymentStatus status;

    @NotNull
    private Instant timestamp;

    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal paymentAmount;
}
