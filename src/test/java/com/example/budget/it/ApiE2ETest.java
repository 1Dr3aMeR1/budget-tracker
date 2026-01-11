package com.example.budget.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiE2ETest extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    // --- DTO для тестов
    record RegisterRequest(String username, String password) {}
    record LoginRequest(String username, String password) {}
    record AuthResponse(String accessToken) {}

    record CreateCategoryRequest(String name, String type) {}
    record CategoryResponse(UUID id, UUID userId, String name, String type, String createdAt) {}

    record CreateTxRequest(UUID categoryId, BigDecimal amount, String type, OffsetDateTime occurredAt, String note) {}
    record TxResponse(UUID id, UUID categoryId, BigDecimal amount, String type, String occurredAt, String note) {}

    record ExpenseByCategory(UUID categoryId, String categoryName, BigDecimal totalAmount) {}

    @Test
    void fullFlow_shouldWork() {
        String username = "user_" + UUID.randomUUID();
        String password = "password123";

        // 1) register
        {
            RegisterRequest req = new RegisterRequest(username, password);
            ResponseEntity<String> res = rest.postForEntity("/api/auth/register", req, String.class);

            if (res.getStatusCode() != HttpStatus.CREATED) {
                throw new AssertionError("REGISTER FAILED\nexpected=201 CREATED, actual=" + res.getStatusCode()
                        + "\nheaders=" + res.getHeaders()
                        + "\nbody=" + res.getBody());
            }
        }

        // 2) login -> token
        String token;
        {
            LoginRequest req = new LoginRequest(username, password);
            ResponseEntity<AuthResponse> res = rest.postForEntity("/api/auth/login", req, AuthResponse.class);
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().accessToken()).isNotBlank();
            token = res.getBody().accessToken();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3) create category
        UUID catId;
        {
            CreateCategoryRequest req = new CreateCategoryRequest("Food", "EXPENSE");
            HttpEntity<CreateCategoryRequest> entity = new HttpEntity<>(req, headers);

            ResponseEntity<CategoryResponse> res =
                    rest.exchange("/api/categories", HttpMethod.POST, entity, CategoryResponse.class);

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(res.getBody()).isNotNull();
            catId = res.getBody().id();
            assertThat(catId).isNotNull();
        }

        // 4) list categories
        {
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<CategoryResponse[]> res =
                    rest.exchange("/api/categories", HttpMethod.GET, entity, CategoryResponse[].class);

            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();
            assertThat(Arrays.stream(res.getBody()).anyMatch(c -> "Food".equals(c.name()))).isTrue();
        }

        // 5) create transaction
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        {
            CreateTxRequest req = new CreateTxRequest(catId, new BigDecimal("12.50"), "EXPENSE", now, "Lunch");
            HttpEntity<CreateTxRequest> entity = new HttpEntity<>(req, headers);

            ResponseEntity<TxResponse> res =
                    rest.exchange("/api/transactions", HttpMethod.POST, entity, TxResponse.class);

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().amount()).isEqualByComparingTo("12.50");
        }

        // 6) list transactions in range (ВАЖНО: UriComponentsBuilder)
        {
            OffsetDateTime from = now.minusDays(1);
            OffsetDateTime to = now.plusDays(1);

            String url = UriComponentsBuilder
                    .fromPath("/api/transactions")
                    .queryParam("from", from)
                    .queryParam("to", to)
                    .build()
                    .toUriString();

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<TxResponse[]> res =
                    rest.exchange(url, HttpMethod.GET, entity, TxResponse[].class);

            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().length).isGreaterThanOrEqualTo(1);
        }

        // 7) analytics expenses-by-category
        {
            OffsetDateTime from = now.minusDays(30);
            OffsetDateTime to = now.plusDays(1);

            String url = UriComponentsBuilder
                    .fromPath("/api/analytics/expenses-by-category")
                    .queryParam("from", from)
                    .queryParam("to", to)
                    .build()
                    .toUriString();

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<ExpenseByCategory[]> res =
                    rest.exchange(url, HttpMethod.GET, entity, ExpenseByCategory[].class);

            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();

            boolean hasFood = Arrays.stream(res.getBody())
                    .anyMatch(x -> "Food".equalsIgnoreCase(x.categoryName()));
            assertThat(hasFood).isTrue();
        }
    }
}