package com.example.budget.api.categories;

import com.example.budget.api.categories.dto.CategoryResponse;
import com.example.budget.api.categories.dto.CreateCategoryRequest;
import com.example.budget.api.categories.dto.UpdateCategoryRequest;
import com.example.budget.application.categories.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest req) {
        return service.create(req);
    }

    @GetMapping
    public List<CategoryResponse> list(@RequestParam UUID userId) {
        return service.list(userId);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
