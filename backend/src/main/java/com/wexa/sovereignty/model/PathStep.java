package com.wexa.sovereignty.model;

/** One walkable step of an access path: "crossed MEMBER_OF into group X". */
public record PathStep(String relType, String nodeId, String nodeType, String nodeName) {
}
