package com.expensetracker.service;

import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.model.ExpenseCreateRequest;
import com.expensetracker.model.ExpenseSummary;
import com.expensetracker.model.MonthlySummary;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public Expense addExpense(ExpenseCreateRequest request) {
        String id = UUID.randomUUID().toString();
        Expense expense = new Expense(
                id,
                request.getTitle(),
                request.getAmount(),
                request.getCategory(),
                request.getDate()
        );
        return repository.save(expense);
    }

    public List<Expense> getExpenses(String category) {
        if (category == null || category.trim().isEmpty()) {
            return repository.findAll();
        }
        return repository.findByCategory(category.trim());
    }

    public Expense getExpenseById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public void deleteExpense(String id) {
        boolean deleted = repository.deleteById(id);
        if (!deleted) {
            throw new ExpenseNotFoundException(id);
        }
    }

    public ExpenseSummary calculateSummary() {
        List<Expense> expenses = repository.findAll();
        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryBreakdown = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return new ExpenseSummary(total, categoryBreakdown);
    }

    public List<MonthlySummary> calculateMonthlySummary() {
        List<Expense> expenses = repository.findAll();

        Map<String, List<Expense>> byMonth = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getDate().format(MONTH_FORMATTER)));

        return byMonth.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<Expense> monthExpenses = entry.getValue();
                    BigDecimal total = monthExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long count = monthExpenses.size();
                    Map<String, BigDecimal> catBreakdown = monthExpenses.stream()
                            .collect(Collectors.groupingBy(
                                    Expense::getCategory,
                                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                            ));
                    return new MonthlySummary(month, total, count, catBreakdown);
                })
                .sorted(Comparator.comparing(MonthlySummary::getMonth).reversed())
                .collect(Collectors.toList());
    }
}
