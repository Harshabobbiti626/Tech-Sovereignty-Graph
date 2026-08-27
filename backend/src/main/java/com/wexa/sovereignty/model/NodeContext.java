package com.wexa.sovereignty.model;

import java.util.List;
import java.util.Map;

public record NodeContext(String id,
                          String type,
                          Map<String, Object> props,
                          List<Dependency> upstream,
                          List<Dependency> downstream) {
}
