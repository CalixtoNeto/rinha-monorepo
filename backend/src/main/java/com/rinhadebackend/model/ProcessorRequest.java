package com.rinhadebackend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ProcessorRequest {
    private UUID correlationId;
    private BigDecimal amount;
    private Instant requestedAt;
    private int attempts = 0;

    public ProcessorRequest(UUID correlationId, BigDecimal amount, Instant requestedAt) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.requestedAt = requestedAt;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}
