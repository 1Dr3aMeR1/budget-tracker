package com.example.budget.api.analytics;

import com.example.budget.api.analytics.dto.ExpenseByCategoryResponse;
import com.example.budget.application.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/expenses-by-category")
    public List<ExpenseByCategoryResponse> expensesByCategory(
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to
    ) {
        return service.expensesByCategory(from, to);
    }
}
