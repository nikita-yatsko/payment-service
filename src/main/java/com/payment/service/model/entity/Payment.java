package com.payment.service.model.entity;

import com.payment.service.model.enums.PaymentStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Field("order_id")
    private String orderId;

    @Field("user_id")
    private String userId;

    private PaymentStatus status;

    private Instant timestamp;

    @Field("payment_amount")
    private BigDecimal paymentAmount;
}
