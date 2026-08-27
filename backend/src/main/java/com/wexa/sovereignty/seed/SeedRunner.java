package com.wexa.sovereignty.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds the database, then exits. Run with:
 *   ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
 */
@Component
@Profile("seed")
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

    private final GraphSeeder seeder;
    private final ConfigurableApplicationContext context;

    public SeedRunner(GraphSeeder seeder, ConfigurableApplicationContext context) {
        this.seeder = seeder;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        seeder.seed();
        seeder.counts().forEach(row ->
                log.info("  {}: {}", row.get("label").asString(), row.get("count").asLong()));
        System.exit(SpringApplication.exit(context));
    }
}
