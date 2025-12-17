package com.payment.service.service;

import com.payment.service.model.dto.PaymentDto;
import com.payment.service.model.dto.PaymentRequest;

import java.time.Instant;
import java.util.List;

public interface PaymentService {

    PaymentDto createPayment(PaymentRequest payment);

    List<PaymentDto> searchByAny(String userId, String orderId, String status);

    Double getTotalPaymentsByUserId(String userId, Instant from, Instant to);

    Double getTotalPaymentsForAllUsers(Instant from, Instant to);
}
