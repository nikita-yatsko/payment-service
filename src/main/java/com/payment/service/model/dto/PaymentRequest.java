package com.payment.service.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String userId;

    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal paymentAmount;
}
