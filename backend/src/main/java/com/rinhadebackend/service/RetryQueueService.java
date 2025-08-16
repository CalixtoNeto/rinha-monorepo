package com.rinhadebackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class RetryQueueService {

    @Autowired
    private PaymentService paymentService;

    @Scheduled(fixedDelay = 1000)
    public void processRetryQueue() {
        List<ProcessorRequest> batch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ProcessorRequest request = paymentService.getRetryQueue().poll();
            if (request == null) break;
            request.incrementAttempts();
            if (request.getAttempts() > 3) continue;
            batch.add(request);
        }
        if (!batch.isEmpty()) {
            Flux.fromIterable(batch)
                    .flatMap(paymentService::processPayment)
                    .onErrorContinue((error, obj) -> {
                        ProcessorRequest failed = (ProcessorRequest) obj;
                        paymentService.getRetryQueue().add(failed);
                    })
                    .subscribe();
        }
    }
}
