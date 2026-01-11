package com.example.budget.it;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("budget")
            .withUsername("budget")
            .withPassword("budget");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        // Flyway должен прогнать миграции на контейнере
        r.add("spring.flyway.enabled", () -> "true");

        // Для тестов лучше так: не ломаем схему, а проверяем что миграции корректны
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        // Чтобы не было конфликтов и "лишнего"
        r.add("spring.main.banner-mode", () -> "off");
    }
}