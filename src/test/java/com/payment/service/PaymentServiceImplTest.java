package com.payment.service;

import com.payment.service.kafka.model.OrderStatus;
import com.payment.service.kafka.service.OrderStatusKafkaService;
import com.payment.service.mapper.PaymentMapper;
import com.payment.service.model.dto.PaymentDto;
import com.payment.service.model.dto.PaymentRequest;
import com.payment.service.model.entity.Payment;
import com.payment.service.model.enums.PaymentStatus;
import com.payment.service.model.exception.InvalidDataException;
import com.payment.service.repository.PaymentRepository;
import com.payment.service.service.impl.PaymentServiceImpl;
import com.payment.service.service.impl.RandomNumberClient;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RandomNumberClient randomClient;

    @Mock
    private OrderStatusKafkaService orderStatusKafkaService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private PaymentRequest paymentRequest;

    private String orderId = "1";
    private String userId = "1";
    private PaymentStatus statusSuccess = PaymentStatus.SUCCESS;
    private PaymentStatus statusFailed = PaymentStatus.FAILED;

    @BeforeEach
    public void setUp() {
        // Payment
        payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setStatus(statusSuccess);
        payment.setPaymentAmount(Decimal128.parse("100.0"));

        // PaymentDto
        paymentDto = new PaymentDto();
        paymentDto.setOrderId(orderId);
        paymentDto.setUserId(userId);
        paymentDto.setStatus(statusSuccess);

        // PaymentRequest
        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(orderId);
        paymentRequest.setUserId(userId);
    }

    @Test
    public void createPayment_success_status() {
        // Arrange:
        when(randomClient.getRandomNumber()).thenReturn(List.of(2));
        when(paymentMapper.createPayment(paymentRequest, statusSuccess)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        doNothing().when(orderStatusKafkaService).sentOrderStatus(any(OrderStatus.class));
        when(paymentMapper.toPaymentDto(payment)).thenReturn(paymentDto);

        // Act:
        PaymentDto result = paymentService.createPayment(paymentRequest);

        // Assert:
        assertEquals(orderId, result.getOrderId());
        assertEquals(userId, result.getUserId());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());

        // Verify:
        verify(randomClient, times(1)).getRandomNumber();
        verify(paymentMapper, times(1)).createPayment(paymentRequest, statusSuccess);
        verify(paymentRepository, times(1)).save(payment);
        verify(orderStatusKafkaService, times(1)).sentOrderStatus(any(OrderStatus.class));
        verify(paymentMapper, times(1)).toPaymentDto(payment);
    }

    @Test
    public void createPayment_failed_status() {
        // Arrange:
        payment.setStatus(statusFailed);
        paymentDto.setStatus(statusFailed);

        when(randomClient.getRandomNumber()).thenReturn(List.of(1));
        when(paymentMapper.createPayment(paymentRequest, statusFailed)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        doNothing().when(orderStatusKafkaService).sentOrderStatus(any(OrderStatus.class));
        when(paymentMapper.toPaymentDto(payment)).thenReturn(paymentDto);

        // Act:
        PaymentDto result = paymentService.createPayment(paymentRequest);

        // Assert:
        assertEquals(orderId, result.getOrderId());
        assertEquals(userId, result.getUserId());
        assertEquals(statusFailed, result.getStatus());

        // Verify:
        verify(randomClient, times(1)).getRandomNumber();
        verify(paymentMapper, times(1)).createPayment(paymentRequest, statusFailed);
        verify(paymentRepository, times(1)).save(payment);
        verify(orderStatusKafkaService, times(1)).sentOrderStatus(any(OrderStatus.class));
        verify(paymentMapper, times(1)).toPaymentDto(payment);
    }

    @Test
    public void searchByAny_emptyInputs_returnsEmptyList() {
        // Act:
        List<PaymentDto> result = paymentService.searchByAny(null, null, null);

        // Assert:
        assertTrue(result.isEmpty());

        // Verify:
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    public void searchByAny_userIdOnly() {
        // Arrange:
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))) .thenReturn(List.of(payment));
        when(paymentMapper.toPaymentDto(payment)).thenReturn(paymentDto);

        // Act:
        List<PaymentDto> result = paymentService.searchByAny("1", null, null);

        // Assert:
        assertEquals(1, result.size());
        assertEquals("1", result.getFirst().getUserId());

        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Payment.class));
        verify(paymentMapper, times(1)).toPaymentDto(payment);
    }

    @Test
    public void searchByAny_orderIdOnly() {
        // Arrange:
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))) .thenReturn(List.of(payment));
        when(paymentMapper.toPaymentDto(payment)).thenReturn(paymentDto);

        // Act:
        List<PaymentDto> result = paymentService.searchByAny(null, "1", null);

        // Assert:
        assertEquals(1, result.size());
        assertEquals("1", result.getFirst().getOrderId());

        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Payment.class));
        verify(paymentMapper, times(1)).toPaymentDto(payment);
    }

    @Test
    public void searchByAny_statusOnly_failed() {
        // Arrange:
        paymentDto.setStatus(statusFailed);

        when(mongoTemplate.find(any(Query.class), eq(Payment.class))) .thenReturn(List.of(payment));
        when(paymentMapper.toPaymentDto(payment)).thenReturn(paymentDto);

        // Act:
        List<PaymentDto> result = paymentService.searchByAny(null, null, "FAILED");

        // Assert:
        assertEquals(1, result.size());
        assertEquals(PaymentStatus.FAILED, result.getFirst().getStatus());

        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Payment.class));
        verify(paymentMapper, times(1)).toPaymentDto(payment);
    }

    @Test
    public void getTotalPaymentsByUserId_success() {
        // Arrange:
        Payment payment1 = new Payment();
        payment1.setUserId("2");
        payment1.setOrderId("1");
        payment1.setPaymentAmount(Decimal128.parse("100.0"));

        Payment payment2 = new Payment();
        payment2.setUserId("2");
        payment2.setOrderId("2");
        payment2.setPaymentAmount(Decimal128.parse("200.0"));

        when(paymentRepository.findAllByUserIdAndTimestampBetween("2", null, null))
                .thenReturn(List.of(payment1, payment2));

        // Act:
        Double result = paymentService.getTotalPaymentsByUserId("2", null, null);

        // Assert:
        assertEquals(300.0, result);

        // Verify:
        verify(paymentRepository, times(1)).findAllByUserIdAndTimestampBetween("2", null, null);
    }

    @Test
    public void getTotalPaymentsByUserId_userNotFound() {
        // Act + Assert:
        assertThrows(InvalidDataException.class, () -> paymentService.getTotalPaymentsByUserId("", null, null));
    }

    @Test
    public void getTotalPaymentsForAllUsers_success() {
        // Arrange:
        Payment payment1 = new Payment();
        payment1.setUserId("2");
        payment1.setOrderId("1");
        payment1.setPaymentAmount(Decimal128.parse("100.0"));
        payment1.setTimestamp(Instant.now().minus(1, ChronoUnit.DAYS));

        Payment payment2 = new Payment();
        payment2.setUserId("2");
        payment2.setOrderId("2");
        payment2.setPaymentAmount(Decimal128.parse("200.0"));
        payment2.setTimestamp(Instant.now().minus(1, ChronoUnit.DAYS));

        when(paymentRepository.findAllByTimestampBetween(null, null)).thenReturn(List.of(payment, payment1, payment2));

        // Act:
        Double result = paymentService.getTotalPaymentsForAllUsers(null, null);

        // Assert:
        assertEquals(400.0, result);

        // Verify:
        verify(paymentRepository, times(1)).findAllByTimestampBetween(null, null);
    }
}
