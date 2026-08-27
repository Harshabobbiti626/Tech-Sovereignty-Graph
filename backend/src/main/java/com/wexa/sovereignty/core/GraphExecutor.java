package com.wexa.sovereignty.core;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Function;

/**
 * Single choke point between Cypher and the database: circuit breaker in,
 * clean HTTP failures out.
 */
@Component
public class GraphExecutor {

    public static final String DB_DOWN = "CognoDB gateway is re-establishing the connection. Try again shortly.";

    private final Driver driver;
    private final CircuitBreaker breaker;

    public GraphExecutor(Driver driver, CircuitBreaker breaker) {
        this.driver = driver;
        this.breaker = breaker;
    }

    public <T> List<T> read(String cypher, Map<String, Object> params, Function<Record, T> mapper) {
        return execute(session -> session.executeRead(tx -> tx.run(cypher, params).list(mapper)));
    }

    /** Direct liveness probe that ignores the breaker — health should report reality. */
    public OptionalLong probe() {
        long start = System.nanoTime();
        try (Session session = driver.session()) {
            session.executeRead(tx -> tx.run(Cypher.PING).single());
            return OptionalLong.of((System.nanoTime() - start) / 1_000_000);
        } catch (Exception e) {
            return OptionalLong.empty();
        }
    }

    public CircuitBreaker breaker() {
        return breaker;
    }

    private <T> T execute(Function<Session, T> work) {
        if (breaker.isOpen()) {
            throw unavailable(null);
        }        try (Session session = driver.session()) {
            T result = work.apply(session);
            breaker.recordSuccess();
            return result;
        } catch (ServiceUnavailableException | SessionExpiredException e) {
            breaker.recordFailure();
            throw unavailable(e);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database credentials were rejected");
        } catch (ClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Cypher query failed: " + e.getMessage());
        }
    }

    private ResponseStatusException unavailable(Throwable cause) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, DB_DOWN, cause);
    }
}
