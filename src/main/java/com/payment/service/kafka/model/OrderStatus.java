package com.payment.service.kafka.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatus {

    private String orderId;
    private String userId;
    private String orderStatus;
}
