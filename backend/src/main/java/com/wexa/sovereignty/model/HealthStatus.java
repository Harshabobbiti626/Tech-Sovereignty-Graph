package com.wexa.sovereignty.model;

public record HealthStatus(String status, boolean database, long latencyMs, boolean breakerOpen) {
}
