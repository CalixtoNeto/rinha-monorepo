package com.rinhadebackend.service;

import com.rinhadebackend.model.PaymentRequest;
import com.rinhadebackend.model.PaymentSummaryResponse;
import com.rinhadebackend.model.ProcessorRequest;
import com.rinhadebackend.model.ProcessorResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PaymentService {

    @Autowired
    private WebClient defaultProcessorWebClient;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private Retry retry;

    @Autowired
    private CircuitBreaker circuitBreaker;

    private final ConcurrentLinkedQueue<ProcessorRequest> retryQueue = new ConcurrentLinkedQueue<>();

    public void processPaymentAsync(PaymentRequest request) {
        ProcessorRequest processorRequest = new ProcessorRequest(request.getCorrelationId(), request.getAmount(), Instant.now());
        processPayment(processorRequest)
                .subscribeOn(Schedulers.parallel())
                .subscribe(
                        success -> {},
                        error -> retryQueue.add(processorRequest)
                );
    }

    public Mono<Void> processPayment(ProcessorRequest request) {
        Mono<ProcessorResponse> responseMono = defaultProcessorWebClient.post()
                .bodyValue(request)
                .retrieve()
                .toEntity(ProcessorResponse.class)
                .map(responseEntity -> responseEntity.getBody());

        return Mono.defer(() -> responseMono)
                .transformDeferred(Retry.decorateMono(retry))
                .transformDeferred(CircuitBreaker.decorateMono(circuitBreaker))
                .flatMap(response -> {
                    if (response.is5xxServerError()) {
                        return Mono.error(new RuntimeException("5xx error"));
                    }
                    return storeInRedis(request);
                });
    }

    private Mono<Void> storeInRedis(ProcessorRequest request) {
        String key = "payments:default";
        String value = request.getAmount().toPlainString() + ":" + request.getCorrelationId();
        double score = request.getRequestedAt().toEpochMilli();
        return redisTemplate.opsForZSet().add(key, value, score).then();
    }

    public Mono<PaymentSummaryResponse> getPaymentSummary(Instant from, Instant to) {
        String key = "payments:default";
        double minScore = from != null ? from.toEpochMilli() : Double.NEGATIVE_INFINITY;
        double maxScore = to != null ? to.toEpochMilli() : Double.POSITIVE_INFINITY;

        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore)
                .collectList()
                .map(members -> {
                    AtomicReference<BigDecimal> sum = new AtomicReference<>(BigDecimal.ZERO);
                    int count = members.size();
                    members.forEach(member -> {
                        String[] parts = member.split(":");
                        sum.updateAndGet(v -> v.add(new BigDecimal(parts[0])));
                    });
                    PaymentSummaryResponse response = new PaymentSummaryResponse();
                    PaymentSummaryResponse.ProcessorSummary defaultSummary = new PaymentSummaryResponse.ProcessorSummary();
                    defaultSummary.setTotalRequests(count);
                    defaultSummary.setTotalAmount(sum.get());
                    response.setDefaultProcessor(defaultSummary);
                    PaymentSummaryResponse.ProcessorSummary fallbackSummary = new PaymentSummaryResponse.ProcessorSummary();
                    fallbackSummary.setTotalRequests(0);
                    fallbackSummary.setTotalAmount(BigDecimal.ZERO);
                    response.setFallback(fallbackSummary);
                    return response;
                });
    }

    public ConcurrentLinkedQueue<ProcessorRequest> getRetryQueue() {
        return retryQueue;
    }
}
