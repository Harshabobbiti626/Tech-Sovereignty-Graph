package com.wexa.sovereignty.model;

import java.util.List;

public record AuditResult(IdentitySummary identity, List<AuditPath> paths, int toxicCount) {
}
