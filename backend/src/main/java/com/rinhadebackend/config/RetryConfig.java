package com.rinhadebackend.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Predicate;

@Configuration
public class RetryConfig {

    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(50))
                .intervalFunction(attempt -> Duration.ofMillis(50L * attempt))
                .retryOnExceptionPredicate(throwable -> throwable instanceof Exception)
                .build();
    }

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .ringBufferSizeInHalfOpenState(10)
                .ringBufferSizeInClosedState(100)
                .recordExceptionPredicate(Predicate.isEqual(true))
                .build();
    }
}
