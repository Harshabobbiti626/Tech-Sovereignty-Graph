package com.wexa.sovereignty.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class Neo4jConfig {

    private static final Logger log = LoggerFactory.getLogger(Neo4jConfig.class);

    /**
     * Created even when CognoDB is asleep — the driver reconnects lazily, so a
     * cold instance means 503s, not a dead API.
     */
    @Bean
    public Driver driver(@Value("${cognodb.uri}") String uri,
                         @Value("${cognodb.user}") String user,
                         @Value("${cognodb.password}") String password) {
        // fail fast into the circuit breaker instead of hanging on the 60s default
        Config config = Config.builder()
                .withConnectionTimeout(10, TimeUnit.SECONDS)
                .withConnectionAcquisitionTimeout(10, TimeUnit.SECONDS)
                .withMaxConnectionPoolSize(10)
                .build();
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
        try {
            driver.verifyConnectivity();
            log.info("Connected to CognoDB at {}", uri);
        } catch (Exception e) {
            log.warn("CognoDB unreachable at startup ({}). The API will keep serving and retry on demand.",
                    e.getMessage());
        }
        return driver;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.split(","))
                        .allowedMethods("GET");
            }
        };
    }
}
