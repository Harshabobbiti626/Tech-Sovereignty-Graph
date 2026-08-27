package com.wexa.sovereignty.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircuitBreakerTest {

    private final CircuitBreaker breaker = new CircuitBreaker(3, 40);

    @Test
    void staysClosedBelowThreshold() {
        breaker.recordFailure();
        breaker.recordFailure();
        assertFalse(breaker.isOpen());
        assertEquals(0, breaker.retryInMillis());
    }

    @Test
    void opensOnConsecutiveFailuresAndFailsFast() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        assertTrue(breaker.isOpen());
        assertTrue(breaker.retryInMillis() > 0);
    }

    @Test
    void successResetsTheFailureCount() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordSuccess();
        breaker.recordFailure();
        assertFalse(breaker.isOpen());
    }

    @Test
    void aFailedProbeReopensImmediately() throws InterruptedException {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        Thread.sleep(60); // let the cooldown lapse — next call is the probe
        assertFalse(breaker.isOpen());
        breaker.recordFailure();
        assertTrue(breaker.isOpen());
    }
}
