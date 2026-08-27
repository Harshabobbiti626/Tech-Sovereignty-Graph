package com.wexa.sovereignty.model;

/**
 * One walkable step of an access path: "crossed MEMBER_OF into group X".
 * Level is only set on ACCESS steps (READ / WRITE / ADMIN).
 */
public record PathStep(String relType, String level, String nodeId, String nodeType, String nodeName) {
}
