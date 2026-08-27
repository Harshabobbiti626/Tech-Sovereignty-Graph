package com.wexa.sovereignty.service;

import com.wexa.sovereignty.core.Cypher;
import com.wexa.sovereignty.core.GraphExecutor;
import com.wexa.sovereignty.model.AuditPath;
import com.wexa.sovereignty.model.AuditResult;
import com.wexa.sovereignty.model.Dependency;
import com.wexa.sovereignty.model.GraphEdge;
import com.wexa.sovereignty.model.GraphNode;
import com.wexa.sovereignty.model.GraphPayload;
import com.wexa.sovereignty.model.GraphStats;
import com.wexa.sovereignty.model.HealthStatus;
import com.wexa.sovereignty.model.IdentitySummary;
import com.wexa.sovereignty.model.ImpactResult;
import com.wexa.sovereignty.model.ImpactRow;
import com.wexa.sovereignty.model.NodeContext;
import com.wexa.sovereignty.model.PathStep;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Function;

@Service
public class GraphService {

    private final GraphExecutor executor;

    public GraphService(GraphExecutor executor) {
        this.executor = executor;
    }

    @Cacheable("graph")
    public GraphPayload graph() {
        List<GraphNode> nodes = executor.read(Cypher.ALL_NODES, Map.of(), row -> new GraphNode(
                row.get("id").asString(),
                row.get("type").asString(),
                row.get("props").asMap()));

        List<GraphEdge> edges = executor.read(Cypher.ALL_EDGES, Map.of(), row -> new GraphEdge(
                edgeId(row),
                row.get("source").asString(),
                row.get("target").asString(),
                row.get("type").asString(),
                row.get("props").asMap()));

        return new GraphPayload(nodes, edges);
    }

    @Cacheable("stats")
    public GraphStats stats() {
        return executor.read(Cypher.STATS, Map.of(), row -> new GraphStats(
                row.get("identities").asLong(),
                row.get("groups").asLong(),
                row.get("resources").asLong(),
                row.get("criticalResources").asLong(),
                row.get("toxicPaths").asLong())).get(0);
    }

    /**
     * Query A. An identity that exists but reaches nothing yields an empty path
     * list (the UI shows its "all clear" state); an unknown email is a 404.
     */
    public AuditResult audit(String email, String resource) {
        IdentitySummary identity = executor.read(Cypher.IDENTITY_BY_EMAIL, Map.of("email", email), row ->
                        new IdentitySummary(row.get("email").asString(),
                                row.get("role").asString(),
                                row.get("status").asString()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown identity: " + email));

        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("resource", resource);

        List<AuditPath> paths = executor.read(Cypher.ACCESS_PATHS, params, this::toAuditPath);
        long toxic = paths.stream().filter(AuditPath::toxic).count();
        return new AuditResult(identity, paths, (int) toxic);
    }

    /** Query B. */
    public ImpactResult impact(String groupName) {
        requireGroup(groupName);

        List<ImpactRow> affected = executor.read(Cypher.BLAST_RADIUS, Map.of("groupName", groupName), row ->
                new ImpactRow(row.get("resourceId").asString(),
                        row.get("resource").asString(),
                        row.get("sensitivity").asString(),
                        row.get("pathsAtRisk").asLong()));

        return new ImpactResult(groupName, affected);
    }

    public NodeContext nodeContext(String id) {
        List<Record> self = executor.read(Cypher.NODE_DETAIL, Map.of("id", id), Function.identity());
        if (self.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown node: " + id);
        }
        Record me = self.get(0);
        return new NodeContext(
                id,
                me.get("type").asString(),
                me.get("props").asMap(),
                executor.read(Cypher.NODE_UPSTREAM, Map.of("id", id), GraphService::toDependency),
                executor.read(Cypher.NODE_DOWNSTREAM, Map.of("id", id), GraphService::toDependency));
    }

    public HealthStatus health() {
        OptionalLong latency = executor.probe();
        boolean up = latency.isPresent();
        return new HealthStatus(up ? "ok" : "degraded", up, latency.orElse(-1), executor.breaker().isOpen());
    }

    private AuditPath toAuditPath(Record row) {
        Path path = row.get("path").asPath();

        List<PathStep> steps = new ArrayList<>();
        var nodes = path.nodes().iterator();
        Node start = nodes.next();
        steps.add(new PathStep(null, null, nodeId(start), label(start), displayName(start)));
        for (Relationship rel : path.relationships()) {
            Node node = nodes.next();
            steps.add(new PathStep(rel.type(), levelOf(rel), nodeId(node), label(node), displayName(node)));
        }

        String sensitivity = row.get("sensitivity").asString();
        boolean suspended = "Suspended".equals(row.get("status").asString());
        return new AuditPath(path.length(),
                suspended && "Critical".equals(sensitivity),
                row.get("resourceId").asString(),
                row.get("resourceName").asString(),
                sensitivity,
                steps);
    }

    private static Dependency toDependency(Record row) {
        return new Dependency(row.get("id").asString(),
                row.get("type").asString(),
                row.get("name").asString(),
                row.get("rel").asString());
    }

    private void requireGroup(String groupName) {
        List<Record> found = executor.read(Cypher.GROUP_BY_NAME, Map.of("name", groupName), Function.identity());
        if (found.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown group: " + groupName);
        }
    }

    private static String levelOf(Relationship rel) {
        Value level = rel.get("level");
        return level.isNull() ? null : level.asString();
    }

    private static String nodeId(Node node) {
        return node.get("id").asString();
    }

    private static String label(Node node) {
        return node.labels().iterator().next();
    }

    private static String displayName(Node node) {
        Value name = node.get("name");
        if (name.isNull()) {
            name = node.get("email");
        }
        return name.isNull() ? nodeId(node) : name.asString();
    }

    private static String edgeId(Record row) {
        return row.get("source").asString() + ":" + row.get("type").asString() + ":" + row.get("target").asString();
    }
}
