package com.wexa.sovereignty.model;

import java.util.List;

public record AuditPath(int length,
                        boolean toxic,
                        String resourceId,
                        String resourceName,
                        String sensitivity,
                        List<PathStep> steps) {
}
