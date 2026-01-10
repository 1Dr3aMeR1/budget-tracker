package com.example.budget.api.transactions;

import com.example.budget.api.transactions.dto.CreateTransactionRequest;
import com.example.budget.api.transactions.dto.TransactionResponse;
import com.example.budget.application.transactions.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody CreateTransactionRequest req) {
        return service.create(req);
    }

    @GetMapping
    public List<TransactionResponse> list(
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to
    ) {
        return service.list(from, to);
    }
}
