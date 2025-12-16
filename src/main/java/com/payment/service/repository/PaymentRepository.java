package com.payment.service.repository;

import com.payment.service.model.entity.Payment;
import com.payment.service.model.enums.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Payment create(Payment payment);

    List<Payment> findAllByUserId(String userId);

    List<Payment> findAllByOrderId(String orderId);

    List<Payment> findAllByStatus(PaymentStatus status);

    BigDecimal sumForUserIdInRange(String userId, Instant from, Instant to);

    BigDecimal sumAllInRange(Instant from, Instant to);
}
