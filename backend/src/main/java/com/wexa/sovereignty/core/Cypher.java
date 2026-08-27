package com.wexa.sovereignty.core;

/**
 * Every statement the API runs. Fixed strings + named parameters — no concatenation, ever.
 */
public final class Cypher {

    private Cypher() {
    }

    /** Most severe first: Critical -> High -> Medium -> Low, then shortest path. */
    static final String SEVERITY = "CASE r.sensitivity WHEN 'Critical' THEN 0 WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END";

    /** Query A — every access path a person has to a resource, direct or via nested groups. */
    public static final String ACCESS_PATHS = """
            MATCH path = (u:Identity {email: $email})-[:MEMBER_OF|INHERITS*1..5]->(g)-[:ACCESS]->(r:Resource)
            WHERE $resource IS NULL OR r.name = $resource
            RETURN path,
                   u.email AS email, u.role AS role, u.status AS status,
                   r.id AS resourceId, r.name AS resourceName, r.sensitivity AS sensitivity
            ORDER BY %s, length(path)
            """.formatted(SEVERITY);

    /** Query B — what goes dark if this group's credentials were revoked (the SQL-awkward one). */
    public static final String BLAST_RADIUS = """
            MATCH (target:Group {name: $groupName})
            MATCH (target)-[:INHERITS*0..3]->(downstream)-[:ACCESS]->(r:Resource)
            RETURN r.id AS resourceId, r.name AS resource, r.sensitivity AS sensitivity,
                   count(DISTINCT downstream) AS pathsAtRisk
            ORDER BY %s, pathsAtRisk DESC
            """.formatted(SEVERITY);

    /** A browser canvas can't draw unbounded graphs, so snapshots are capped. */
    private static final int SNAPSHOT_LIMIT = 2000;

    /** Full picture for the canvas. Types are lowercased for stable API casing. */
    public static final String ALL_NODES =
            "MATCH (n) RETURN n.id AS id, toLower(labels(n)[0]) AS type, properties(n) AS props LIMIT " + SNAPSHOT_LIMIT;

    public static final String ALL_EDGES = "MATCH (a)-[r]->(b) "
            + "RETURN a.id AS source, b.id AS target, type(r) AS type, properties(r) AS props LIMIT " + SNAPSHOT_LIMIT;

    /** Drawer detail: the node itself plus one hop in each direction. */
    public static final String NODE_DETAIL =
            "MATCH (n) WHERE n.id = $id RETURN n.id AS id, toLower(labels(n)[0]) AS type, properties(n) AS props";

    public static final String NODE_UPSTREAM = """
            MATCH (src)-[r]->(n) WHERE n.id = $id
            RETURN src.id AS id, labels(src)[0] AS type, coalesce(src.name, src.email) AS name, type(r) AS rel
            """;

    public static final String NODE_DOWNSTREAM = """
            MATCH (n)-[r]->(dst) WHERE n.id = $id
            RETURN dst.id AS id, labels(dst)[0] AS type, coalesce(dst.name, dst.email) AS name, type(r) AS rel
            """;

    /** Header cards, including the headline number: suspended identities reaching critical data. */
    public static final String STATS = """
            MATCH (i:Identity) WITH count(i) AS identities
            MATCH (g:Group)    WITH identities, count(g) AS groups
            MATCH (r:Resource) WITH identities, groups, count(r) AS resources,
                                  count(CASE WHEN r.sensitivity = 'Critical' THEN 1 END) AS criticalResources
            OPTIONAL MATCH p = (:Identity {status: 'Suspended'})-[:MEMBER_OF|INHERITS*1..5]->(:Group)
                              -[:ACCESS]->(:Resource {sensitivity: 'Critical'})
            RETURN identities, groups, resources, criticalResources, count(DISTINCT p) AS toxicPaths
            """;

    /** Existence checks so "unknown identity" (404) differs from "no access paths" (empty result). */
    public static final String IDENTITY_BY_EMAIL =
            "MATCH (u:Identity {email: $email}) RETURN u.email AS email, u.role AS role, u.status AS status";

    public static final String GROUP_BY_NAME = "MATCH (g:Group {name: $name}) RETURN g.name AS name";

    /** Liveness probe for /api/health. */
    public static final String PING = "RETURN 1";
}
