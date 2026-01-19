package com.payment.service.repository;

import com.payment.service.model.entity.Payment;
import com.payment.service.model.enums.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findAllByUserId(String userId);

    List<Payment> findAllByOrderId(String orderId);

    List<Payment> findAllByStatus(PaymentStatus status);

    List<Payment> findAllByUserIdAndTimestampBetween(String userId, Instant from, Instant to);

    List<Payment> findAllByTimestampBetween(Instant from, Instant to);

}
