package com.wexa.sovereignty.service;

import com.wexa.sovereignty.core.Cypher;
import com.wexa.sovereignty.core.GraphExecutor;
import com.wexa.sovereignty.model.AuditResult;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphServiceTest {

    private final GraphExecutor executor = mock(GraphExecutor.class);
    private final GraphService service = new GraphService(executor);

    @Test
    @SuppressWarnings("unchecked")
    void auditFlagsSuspendedIdentityReachingCriticalResource() {
        Record identity = row(Map.of(
                "email", Values.value("former_vendor_consultant@external.com"),
                "role", Values.value("Vendor Consultant"),
                "status", Values.value("Suspended")));

        // consultant -> Legacy -> EngRO -> DbAdmin -[ADMIN]-> PII
        Path path = path(
                node("id_vendor_consultant", "Identity", null, "former_vendor_consultant@external.com"),
                step("MEMBER_OF", null, "gr_legacy_ext", "Group", "Legacy_External_Contractors"),
                step("INHERITS", null, "gr_eng_ro", "Group", "Engineering_Read_Only"),
                step("INHERITS", null, "gr_db_admin", "Group", "Global_Database_Admin"),
                step("ACCESS", "ADMIN", "res_pii", "Resource", "Customer_PII_Database"));
        Record pathRow = row(Map.of(
                "path", pathValue(path),
                "status", Values.value("Suspended"),
                "sensitivity", Values.value("Critical"),
                "resourceId", Values.value("res_pii"),
                "resourceName", Values.value("Customer_PII_Database")));

        when(executor.read(eq(Cypher.IDENTITY_BY_EMAIL), anyMap(), any()))
                .thenAnswer(inv -> List.of(((Function<Record, Object>) inv.getArgument(2)).apply(identity)));
        when(executor.read(eq(Cypher.ACCESS_PATHS), anyMap(), any()))
                .thenAnswer(inv -> List.of(((Function<Record, Object>) inv.getArgument(2)).apply(pathRow)));

        AuditResult result = service.audit("former_vendor_consultant@external.com", null);

        assertEquals("Suspended", result.identity().status());
        assertEquals(1, result.paths().size());
        assertEquals(1, result.toxicCount());
        assertTrue(result.paths().get(0).toxic());

        var steps = result.paths().get(0).steps();
        assertEquals(5, steps.size());
        assertEquals(List.of("MEMBER_OF", "INHERITS", "INHERITS", "ACCESS"),
                steps.stream().skip(1).map(s -> s.relType()).toList());
        assertEquals("ADMIN", steps.get(4).level());
        assertEquals("Customer_PII_Database", steps.get(4).nodeName());
    }

    @Test
    void auditReturns404ForUnknownIdentity() {
        when(executor.read(eq(Cypher.IDENTITY_BY_EMAIL), anyMap(), any())).thenAnswer(inv -> List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.audit("nobody@wexa.ai", null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ---------------------------------------------------------------- helpers

    private static Record row(Map<String, Value> values) {
        Record record = mock(Record.class);
        when(record.get(any(String.class)))
                .thenAnswer(inv -> values.getOrDefault(inv.getArgument(0), Values.NULL));
        return record;
    }

    private static Node node(String id, String label, String name, String email) {
        Node node = mock(Node.class);
        when(node.get("id")).thenReturn(Values.value(id));
        when(node.labels()).thenReturn(List.of(label));
        when(node.get("name")).thenReturn(name != null ? Values.value(name) : Values.NULL);
        when(node.get("email")).thenReturn(email != null ? Values.value(email) : Values.NULL);
        return node;
    }

    private record Step(String type, String level, Node to) {
    }

    /** Values.value(Path) iterates the path internally, so mock the wrapper instead. */
    private static Value pathValue(Path path) {
        Value value = mock(Value.class);
        when(value.asPath()).thenReturn(path);
        return value;
    }

    private static Step step(String type, String level, String toId, String toLabel, String toName) {
        return new Step(type, level, node(toId, toLabel, toName, null));
    }

    private static Path path(Node start, Step... steps) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(start);
        List<Relationship> rels = new ArrayList<>();
        for (Step step : steps) {
            Relationship rel = mock(Relationship.class);
            when(rel.type()).thenReturn(step.type());
            when(rel.get("level")).thenReturn(step.level() != null ? Values.value(step.level()) : Values.NULL);
            rels.add(rel);
            nodes.add(step.to());
        }
        Path path = mock(Path.class);
        when(path.nodes()).thenReturn(nodes);
        when(path.relationships()).thenReturn(rels);
        when(path.length()).thenReturn(rels.size());
        return path;
    }
}
