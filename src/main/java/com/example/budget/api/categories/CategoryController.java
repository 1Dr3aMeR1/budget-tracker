package com.example.budget.api.categories;

import com.example.budget.api.categories.dto.CategoryResponse;
import com.example.budget.api.categories.dto.CreateCategoryRequest;
import com.example.budget.api.categories.dto.UpdateCategoryRequest;
import com.example.budget.application.categories.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.example.budget.infrastructure.security.JwtAuthFilter.AuthPrincipal;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest req, Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return service.create(p.userId(), req);
    }

    @GetMapping
    public List<CategoryResponse> list(Authentication auth) {
        AuthPrincipal p = (AuthPrincipal) auth.getPrincipal();
        return service.list(p.userId());
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest req) {
        return service.update(currentUserId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(currentUserId(), id);
    }

    private UUID currentUserId() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(userId);
    }
}