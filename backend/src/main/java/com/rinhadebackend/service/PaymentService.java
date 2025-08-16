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
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PaymentService {

    @Autowired
    private WebClient defaultProcessorWebClient;

    @Autowired
    private WebClient fallbackProcessorWebClient;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private Retry retry;

    @Autowired
    private CircuitBreaker circuitBreaker;

    private final ConcurrentLinkedQueue<ProcessorRequest> retryQueue = new ConcurrentLinkedQueue<>();

    public void processPaymentAsync(PaymentRequest request) {
        ProcessorRequest processorRequest = new ProcessorRequest(
                request.getCorrelationId(),
                request.getAmount(),
                Instant.now()
        );
        processPayment(processorRequest)
                .subscribeOn(Schedulers.parallel())
                .subscribe(
                        success -> {},
                        error -> retryQueue.add(processorRequest)
                );
    }

    public Mono<Void> processPayment(ProcessorRequest request) {
        return sendToProcessor(defaultProcessorWebClient, "payments:default", request)
                .onErrorResume(error -> sendToProcessor(fallbackProcessorWebClient, "payments:fallback", request));
    }

    private Mono<Void> sendToProcessor(WebClient client, String key, ProcessorRequest request) {
        Mono<ProcessorResponse> responseMono = client.post()
                .bodyValue(request)
                .retrieve()
                .toEntity(ProcessorResponse.class)
                .map(entity -> {
                    ProcessorResponse body = entity.getBody();
                    body.setStatusCode(entity.getStatusCode());
                    return body;
                });

        return Mono.defer(() -> responseMono)
                .transformDeferred(Retry.decorateMono(retry))
                .transformDeferred(CircuitBreaker.decorateMono(circuitBreaker))
                .flatMap(response -> {
                    if (response.is5xxServerError()) {
                        return Mono.error(new RuntimeException("5xx error"));
                    }
                    return storeInRedis(key, request);
                });
    }

    private Mono<Void> storeInRedis(String key, ProcessorRequest request) {
        String value = request.getAmount().toPlainString() + ":" + request.getCorrelationId();
        double score = request.getRequestedAt().toEpochMilli();
        return redisTemplate.opsForZSet().add(key, value, score).then();
    }

    public Mono<PaymentSummaryResponse> getPaymentSummary(Instant from, Instant to) {
        String defaultKey = "payments:default";
        String fallbackKey = "payments:fallback";
        double minScore = from != null ? from.toEpochMilli() : Double.NEGATIVE_INFINITY;
        double maxScore = to != null ? to.toEpochMilli() : Double.POSITIVE_INFINITY;

        Mono<List<String>> defaultData = redisTemplate.opsForZSet()
                .rangeByScore(defaultKey, minScore, maxScore)
                .collectList();
        Mono<List<String>> fallbackData = redisTemplate.opsForZSet()
                .rangeByScore(fallbackKey, minScore, maxScore)
                .collectList();

        return Mono.zip(defaultData, fallbackData)
                .map(tuple -> {
                    List<String> defaultMembers = tuple.getT1();
                    List<String> fallbackMembers = tuple.getT2();

                    BigDecimal defaultSum = sumAmounts(defaultMembers);
                    BigDecimal fallbackSum = sumAmounts(fallbackMembers);

                    PaymentSummaryResponse response = new PaymentSummaryResponse();
                    PaymentSummaryResponse.ProcessorSummary defaultSummary = new PaymentSummaryResponse.ProcessorSummary();
                    defaultSummary.setTotalRequests(defaultMembers.size());
                    defaultSummary.setTotalAmount(defaultSum);
                    response.setDefaultProcessor(defaultSummary);

                    PaymentSummaryResponse.ProcessorSummary fallbackSummary = new PaymentSummaryResponse.ProcessorSummary();
                    fallbackSummary.setTotalRequests(fallbackMembers.size());
                    fallbackSummary.setTotalAmount(fallbackSum);
                    response.setFallback(fallbackSummary);

                    return response;
                });
    }

    public ConcurrentLinkedQueue<ProcessorRequest> getRetryQueue() {
        return retryQueue;
    }

    private BigDecimal sumAmounts(List<String> members) {
        BigDecimal total = BigDecimal.ZERO;
        for (String member : members) {
            int idx = member.indexOf(':');
            if (idx > 0) {
                total = total.add(new BigDecimal(member.substring(0, idx)));
            }
        }
        return total;
    }
}
