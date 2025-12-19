package com.payment.service.kafka.service;

import com.payment.service.kafka.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusKafkaService {

    private final KafkaTemplate<String, OrderStatus> kafkaTemplate;

    public void sentOrderStatus(OrderStatus response) {
        log.info("Order status: {} sent to order-status with orderId: {}", response.getOrderStatus(), response.getOrderId());
        kafkaTemplate.send("CREATE_PAYMENT", response.getOrderId(), response);
    }
}
