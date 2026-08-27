package com.wexa.sovereignty.seed;

import com.wexa.sovereignty.core.CircuitBreaker;
import com.wexa.sovereignty.core.GraphExecutor;
import com.wexa.sovereignty.service.GraphService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the real seeder against a throwaway Neo4j and exercises the real
 * Cypher through the real service — no mocks on the data path.
 * Skips itself automatically when no Docker daemon is available.
 */
@Testcontainers(disabledWithoutDocker = true)
class Neo4jGraphQueriesTest {

    @Container
    static final Neo4jContainer<?> NEO4J = new Neo4jContainer<>("neo4j:5.26")
            .withAdminPassword("test-password");

    static Driver driver;
    static GraphService service;

    @BeforeAll
    static void startAndSeed() {
        driver = GraphDatabase.driver(NEO4J.getBoltUrl(), AuthTokens.basic("neo4j", "test-password"));
        new GraphSeeder(driver).seed();
        service = new GraphService(new GraphExecutor(driver, new CircuitBreaker(3, 15000)));
    }

    @AfterAll
    static void stop() {
        driver.close();
    }

    @Test
    void auditFindsTheToxicInheritancePath() {
        var result = service.audit("former_vendor_consultant@external.com", null);

        assertEquals(2, result.toxicCount());
        var toxic = result.paths().stream().filter(p -> p.toxic()).toList();
        assertEquals(2, toxic.size());
        assertTrue(toxic.stream().allMatch(p -> p.resourceName().equals("Customer_PII_Database")
                || p.resourceName().equals("AWS_Root_Keys")));
        // consultant -> Legacy -> EngRO -> DbAdmin -> PII is 4 hops with ADMIN at the end
        var piiPath = toxic.stream()
                .filter(p -> p.resourceName().equals("Customer_PII_Database")).findFirst().orElseThrow();
        assertEquals(4, piiPath.length());
        assertEquals("ADMIN", piiPath.steps().get(4).level());
    }

    @Test
    void blastRadiusFlagsCriticalResources() {
        var result = service.impact("Engineering_Read_Only");

        assertTrue(result.affected().stream().anyMatch(row ->
                row.resource().equals("Customer_PII_Database") && row.pathsAtRisk() >= 1));
        assertTrue(result.affected().stream().anyMatch(row ->
                row.resource().equals("AWS_Root_Keys")));
    }

    @Test
    void snapshotMatchesSeededShape() {
        var graph = service.graph();
        assertEquals(40, graph.nodes().size());
        assertEquals(71, graph.edges().size());
    }

    @Test
    void statsCountToxicPaths() {
        var stats = service.stats();
        assertEquals(19, stats.identities());
        assertEquals(4, stats.toxicPaths());
    }
}
