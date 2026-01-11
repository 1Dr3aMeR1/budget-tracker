package com.example.budget.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.UUID;

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

    record CreateTxRequest(UUID categoryId, BigDecimal amount, String type, String occurredAt, String note) {}
    record TxResponse(UUID id, UUID categoryId, BigDecimal amount, String type, String occurredAt, String note) {}

    record ExpenseByCategory(UUID categoryId, String categoryName, BigDecimal totalAmount) {}

    @Test
    void fullFlow_shouldWork() {
        // --- уникальный пользователь
        String username = "user_" + UUID.randomUUID().toString().replace("-", "");
        String password = "password123";

        // 1) register
        {
            RegisterRequest req = new RegisterRequest(username, password);
            ResponseEntity<String> res = rest.postForEntity("/api/auth/register", req, String.class);

            debugIfNot(res, HttpStatus.CREATED, "REGISTER");
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        // 2) login -> token
        String token;
        {
            LoginRequest req = new LoginRequest(username, password);
            ResponseEntity<AuthResponse> res = rest.postForEntity("/api/auth/login", req, AuthResponse.class);

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                System.out.println("LOGIN FAILED");
                System.out.println("status=" + res.getStatusCode());
                System.out.println("headers=" + res.getHeaders());
                System.out.println("body=" + safeBodyAsString("/api/auth/login", req));
            }

            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().accessToken()).isNotBlank();
            token = res.getBody().accessToken();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3) Unauthorized check (без токена должно быть 401/403)
        {
            ResponseEntity<String> res = rest.getForEntity("/api/categories", String.class);
            assertThat(res.getStatusCode().value()).isIn(401, 403);
        }

        // 4) UI доступен (без токена)
        {
            ResponseEntity<String> res = rest.getForEntity("/index.html", String.class);
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().toLowerCase()).contains("<!doctype html");
        }

        // 5) create category
        UUID catId;
        String catName = "Food_" + UUID.randomUUID().toString().substring(0, 8);
        {
            CreateCategoryRequest req = new CreateCategoryRequest(catName, "EXPENSE");
            HttpEntity<CreateCategoryRequest> entity = new HttpEntity<>(req, headers);

            ResponseEntity<CategoryResponse> res =
                    rest.exchange("/api/categories", HttpMethod.POST, entity, CategoryResponse.class);

            debugIfNot(res, HttpStatus.CREATED, "CREATE CATEGORY");
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(res.getBody()).isNotNull();
            catId = res.getBody().id();
            assertThat(catId).isNotNull();
        }

        // 6) list categories
        {
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<CategoryResponse[]> res =
                    rest.exchange("/api/categories", HttpMethod.GET, entity, CategoryResponse[].class);

            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();
            assertThat(Arrays.stream(res.getBody()).anyMatch(c -> catName.equals(c.name()))).isTrue();
        }

        // 7) create transaction
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
        {
            CreateTxRequest req = new CreateTxRequest(
                    catId,
                    new BigDecimal("12.50"),
                    "EXPENSE",
                    nowUtc.toString(),
                    "Lunch"
            );

            HttpEntity<CreateTxRequest> entity = new HttpEntity<>(req, headers);

            ResponseEntity<TxResponse> res =
                    rest.exchange("/api/transactions", HttpMethod.POST, entity, TxResponse.class);

            debugIfNot(res, HttpStatus.CREATED, "CREATE TX");
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().amount()).isEqualByComparingTo("12.50");
        }

        // 8) list transactions in range
        {
            OffsetDateTime from = nowUtc.minusDays(1);
            OffsetDateTime to = nowUtc.plusDays(1);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<TxResponse[]> res =
                    rest.exchange(
                            "/api/transactions?from={from}&to={to}",
                            HttpMethod.GET,
                            entity,
                            TxResponse[].class,
                            from.toString(),
                            to.toString()
                    );

            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().length).isGreaterThanOrEqualTo(1);
        }

        // 9) analytics expenses-by-category
        {
            OffsetDateTime from = nowUtc.minusDays(30);
            OffsetDateTime to = nowUtc.plusDays(1);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ExpenseByCategory[]> res =
                    rest.exchange(
                            "/api/analytics/expenses-by-category?from={from}&to={to}",
                            HttpMethod.GET,
                            entity,
                            ExpenseByCategory[].class,
                            from.toString(),
                            to.toString()
                    );

            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(res.getBody()).isNotNull();

            boolean hasOurCategory = Arrays.stream(res.getBody())
                    .anyMatch(x -> catName.equalsIgnoreCase(x.categoryName()));
            assertThat(hasOurCategory).isTrue();
        }
    }

    private void debugIfNot(ResponseEntity<?> res, HttpStatus expected, String label) {
        if (!res.getStatusCode().equals(expected)) {
            System.out.println(label + " FAILED");
            System.out.println("expected=" + expected + ", actual=" + res.getStatusCode());
            System.out.println("headers=" + res.getHeaders());
            System.out.println("body=" + res.getBody());
        }
    }

    private String safeBodyAsString(String url, Object req) {
        try {
            ResponseEntity<String> res = rest.postForEntity(url, req, String.class);
            return res.getBody();
        } catch (Exception e) {
            return "could not read body: " + e.getMessage();
        }
    }
}