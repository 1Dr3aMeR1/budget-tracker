package com.example.budget.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UnauthorizedAccessTest extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void categoriesShouldBe401or403WithoutToken() {
        ResponseEntity<String> res = rest.getForEntity("/api/categories", String.class);
        assertThat(res.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void transactionsShouldBe401or403WithoutToken() {
        ResponseEntity<String> res = rest.getForEntity(
                "/api/transactions?from=2026-01-01T00:00:00Z&to=2026-01-02T00:00:00Z",
                String.class
        );
        assertThat(res.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void analyticsShouldBe401or403WithoutToken() {
        ResponseEntity<String> res = rest.getForEntity(
                "/api/analytics/expenses-by-category?from=2026-01-01T00:00:00Z&to=2026-01-02T00:00:00Z",
                String.class
        );
        assertThat(res.getStatusCode().value()).isIn(401, 403);
    }
}