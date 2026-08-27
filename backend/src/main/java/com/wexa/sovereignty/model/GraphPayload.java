package com.wexa.sovereignty.model;

import java.util.List;

public record GraphPayload(List<GraphNode> nodes, List<GraphEdge> edges) {
}
