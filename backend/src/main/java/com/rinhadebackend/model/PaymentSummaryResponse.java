package com.rinhadebackend.model;

import java.math.BigDecimal;

public class PaymentSummaryResponse {
    private ProcessorSummary defaultProcessor;
    private ProcessorSummary fallback;

    public ProcessorSummary getDefaultProcessor() {
        return defaultProcessor;
    }

    public void setDefaultProcessor(ProcessorSummary defaultProcessor) {
        this.defaultProcessor = defaultProcessor;
    }

    public ProcessorSummary getFallback() {
        return fallback;
    }

    public void setFallback(ProcessorSummary fallback) {
        this.fallback = fallback;
    }

    public static class ProcessorSummary {
        private int totalRequests;
        private BigDecimal totalAmount;

        public int getTotalRequests() {
            return totalRequests;
        }

        public void setTotalRequests(int totalRequests) {
            this.totalRequests = totalRequests;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
}
