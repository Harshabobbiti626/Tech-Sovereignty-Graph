package com.wexa.sovereignty.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fails fast after N consecutive database failures; one probe call after the
 * cooldown decides whether to close again.
 */
@Component
public class CircuitBreaker {

    private final int failureThreshold;
    private final long cooldownMillis;

    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private final AtomicLong openUntil = new AtomicLong();

    public CircuitBreaker(@Value("${breaker.failure-threshold}") int failureThreshold,
                          @Value("${breaker.cooldown-millis}") long cooldownMillis) {
        this.failureThreshold = failureThreshold;
        this.cooldownMillis = cooldownMillis;
    }

    public boolean isOpen() {
        return openUntil.get() > System.currentTimeMillis();
    }

    public void recordSuccess() {
        consecutiveFailures.set(0);
        openUntil.set(0);
    }

    public void recordFailure() {
        if (consecutiveFailures.incrementAndGet() >= failureThreshold) {
            openUntil.set(System.currentTimeMillis() + cooldownMillis);
        }
    }

    /** Milliseconds until the breaker lets a probe through; 0 when closed. */
    public long retryInMillis() {
        return Math.max(0, openUntil.get() - System.currentTimeMillis());
    }
}
