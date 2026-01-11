package com.example.budget.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SmokeTest extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void uiIndexShouldBeAccessible() {
        ResponseEntity<String> res = rest.getForEntity("/", String.class);

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).containsIgnoringCase("<!doctype html");
    }

    @Test
    void swaggerShouldBeAccessible() {
        ResponseEntity<String> res = rest.getForEntity("/swagger-ui/index.html", String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
    }
}