package com.example.budget.it;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("budget")
            .withUsername("budget")
            .withPassword("budget");

    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        // полезно для стабильности/скорости на CI
        r.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        r.add("spring.datasource.hikari.connection-timeout", () -> "5000");
        r.add("spring.datasource.hikari.validation-timeout", () -> "2000");

        // чтобы миграции всегда отрабатывали на свежей БД
        r.add("spring.flyway.enabled", () -> "true");
    }
}