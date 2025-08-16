package com.rinhadebackend.controller;

import com.rinhadebackend.model.PaymentRequest;
import com.rinhadebackend.model.PaymentSummaryResponse;
import com.rinhadebackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payments")
    public Mono<ResponseEntity<Void>> createPayment(@RequestBody PaymentRequest request) {
        paymentService.processPaymentAsync(request);
        return Mono.just(ResponseEntity.accepted().build());
    }

    @GetMapping("/payments-summary")
    public Mono<PaymentSummaryResponse> getSummary(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        Instant fromInstant = from != null ? Instant.parse(from) : null;
        Instant toInstant = to != null ? Instant.parse(to) : null;
        return paymentService.getPaymentSummary(fromInstant, toInstant);
    }
}
