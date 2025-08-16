package com.rinhadebackend.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public Retry retry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3) // Maximum retry attempts
                .intervalFunction(attempt -> 1000L * attempt) // Exponential backoff: 1s, 2s, 3s
                .build();
        return Retry.of("paymentRetry", config);
    }

    @Bean
    public CircuitBreaker circuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate to open circuit
                .waitDurationInOpenState(Duration.ofSeconds(5)) // Time to wait before transitioning to half-open
                .permittedNumberOfCallsInHalfOpenState(3) // Calls allowed in half-open state
                .slidingWindowSize(10) // Size of sliding window for failure tracking
                .build();
        return CircuitBreaker.of("paymentCircuitBreaker", config);
    }
}