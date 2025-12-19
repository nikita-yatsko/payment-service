package com.payment.service.service.Impl;

import com.payment.service.kafka.service.OrderStatusKafkaService;
import com.payment.service.mapper.PaymentMapper;
import com.payment.service.kafka.model.OrderStatus;
import com.payment.service.model.constant.ErrorMessage;
import com.payment.service.model.dto.PaymentDto;
import com.payment.service.model.dto.PaymentRequest;
import com.payment.service.model.entity.Payment;
import com.payment.service.model.enums.PaymentStatus;
import com.payment.service.model.exception.InvalidDataException;
import com.payment.service.repository.PaymentRepository;
import com.payment.service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MongoTemplate mongoTemplate;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomClient;
    private final OrderStatusKafkaService orderStatusKafkaService;

    @Override
    @Transactional
    public PaymentDto createPayment(PaymentRequest request) {
        PaymentStatus status;

        if (isEvenNumber() )
             status = PaymentStatus.SUCCESS;
        else
            status = PaymentStatus.FAILED;

        Payment paymentRequest = paymentMapper.createPayment(request, status);
        Payment newPayment =  paymentRepository.save(paymentRequest);

        // Send to order-service
        OrderStatus orderStatus = new OrderStatus(
                newPayment.getOrderId(),
                newPayment.getUserId(),
                newPayment.getStatus().name());

        orderStatusKafkaService.sentOrderStatus(orderStatus);

        return paymentMapper.toPaymentDto(newPayment);
    }

    @Override
    public List<PaymentDto> searchByAny(String userId, String orderId, String status) {
        List<Criteria> ors = new ArrayList<>();

        if (userId != null && !userId.isBlank())
            ors.add(Criteria.where("user_id").is(userId));
        if (orderId != null && !orderId.isBlank())
            ors.add(Criteria.where("order_id").is(orderId));
        if (status != null)
            ors.add(Criteria.where("status").is(status));

        if (ors.isEmpty())
            return Collections.emptyList();

        Query query = new Query(new Criteria().andOperator(ors.toArray(new Criteria[0])));

        return mongoTemplate.find(query, Payment.class)
                .stream()
                .map(paymentMapper::toPaymentDto)
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalPaymentsByUserId(String userId, Instant from, Instant to) {
        if (userId == null || userId.isEmpty())
            throw new InvalidDataException(ErrorMessage.USER_ID_NOT_FOUND.getMessage(userId));

        return paymentRepository.findAllByUserIdAndTimestampBetween(userId, from, to)
                .stream()
                .mapToDouble(p -> p.getPaymentAmount().doubleValue())
                .sum();
    }

    @Override
    public Double getTotalPaymentsForAllUsers(Instant from, Instant to) {
        return paymentRepository.findAllByTimestampBetween(from, to)
                .stream()
                .mapToDouble(p -> p.getPaymentAmount().doubleValue())
                .sum();
    }

    private Boolean isEvenNumber() {
        return randomClient.getRandomNumber()
                .stream()
                .findFirst()
                .map(n -> n % 2 == 0)
                .orElse(false);
    }
}