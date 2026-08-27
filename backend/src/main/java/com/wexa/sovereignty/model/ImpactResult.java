package com.wexa.sovereignty.model;

import java.util.List;

public record ImpactResult(String group, List<ImpactRow> affected) {
}
