package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.model.ExpenseCreateRequest;
import com.expensetracker.model.ExpenseSummary;
import com.expensetracker.model.MonthlySummary;
import com.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expenses", description = "Smart Personal Expense Tracker REST API")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Add a new expense", description = "Creates a new expense record with UUID, title, amount, category, and date")
    public ResponseEntity<Expense> addExpense(@Valid @RequestBody ExpenseCreateRequest request) {
        Expense created = service.addExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "View all expenses", description = "Retrieves all expenses, optionally filtered by category")
    public ResponseEntity<List<Expense>> getExpenses(
            @Parameter(description = "Filter expenses by category (case-insensitive)")
            @RequestParam(required = false) String category) {
        List<Expense> expenses = service.getExpenses(category);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/summary")
    @Operation(summary = "Calculate total expenses", description = "Calculates overall total expenses and breakdown by category")
    public ResponseEntity<ExpenseSummary> getSummary() {
        ExpenseSummary summary = service.calculateSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/monthly")
    @Operation(summary = "Calculate monthly summary (Bonus)", description = "Calculates expense totals and category breakdowns grouped by year and month")
    public ResponseEntity<List<MonthlySummary>> getMonthlySummary() {
        List<MonthlySummary> monthlySummaries = service.calculateMonthlySummary();
        return ResponseEntity.ok(monthlySummaries);
    }

    @GetMapping("/{id}")
    @Operation(summary = "View a single expense", description = "Retrieves an expense by its unique ID")
    public ResponseEntity<Expense> getExpenseById(
            @Parameter(description = "Unique UUID of the expense")
            @PathVariable String id) {
        Expense expense = service.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense", description = "Deletes an expense by its unique ID")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "Unique UUID of the expense to delete")
            @PathVariable String id) {
        service.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
