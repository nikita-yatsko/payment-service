package com.payment.service.controllers;

import com.payment.service.model.dto.PaymentDto;
import com.payment.service.model.dto.PaymentRequest;
import com.payment.service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentDto> createPayment(
            @RequestBody @Valid PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    @GetMapping("/byAny")
    @PreAuthorize("userId == principal.id.toString() or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDto>> getPaymentsByAny(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String status ) {

        return ResponseEntity.ok(paymentService.searchByAny(userId, orderId, status));
    }

    @GetMapping("/summary")
    @PreAuthorize("userId == principal.id.toString() or hasRole('ADMIN')")
    public ResponseEntity<Double> getTotalSum(
            @RequestParam("userId") String userId,
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to ) {
        return ResponseEntity.ok(paymentService.getTotalPaymentsByUserId(userId, from, to));
    }

    @GetMapping("/summary/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Double> getTotalSumAll(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to ) {
        return ResponseEntity.ok(paymentService.getTotalPaymentsForAllUsers(from, to));
    }
}
