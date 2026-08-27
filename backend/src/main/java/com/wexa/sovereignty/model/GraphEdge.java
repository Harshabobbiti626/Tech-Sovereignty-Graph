package com.wexa.sovereignty.model;

import java.util.Map;

/** Edge ids follow the "source:type:target" convention so the UI can match them to audit paths. */
public record GraphEdge(String id, String source, String target, String type, Map<String, Object> props) {
}
