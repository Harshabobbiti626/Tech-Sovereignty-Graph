package com.wexa.sovereignty.model;

public record GraphStats(long identities,
                         long groups,
                         long resources,
                         long criticalResources,
                         long toxicPaths) {
}
