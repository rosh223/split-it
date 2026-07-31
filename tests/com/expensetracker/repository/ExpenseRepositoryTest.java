package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseRepositoryTest {

    @TempDir
    Path tempDir;

    private ExpenseRepository repository;

    @BeforeEach
    void setUp() {
        File tempFile = tempDir.resolve("test-expenses.json").toFile();
        repository = new ExpenseRepository(tempFile.getAbsolutePath());
        repository.init();
    }

    @AfterEach
    void tearDown() {
        repository.clearAll();
    }

    @Test
    void save_shouldAddAndPersistExpense() {
        Expense expense = new Expense("id-1", "Lunch", new BigDecimal("15.50"), "Food", LocalDate.of(2026, 7, 31));

        Expense saved = repository.save(expense);

        assertThat(saved).isEqualTo(expense);
        assertThat(repository.findAll()).hasSize(1).contains(expense);
    }

    @Test
    void save_shouldUpdateExistingExpense() {
        Expense expense = new Expense("id-1", "Lunch", new BigDecimal("15.50"), "Food", LocalDate.of(2026, 7, 31));
        repository.save(expense);

        Expense updated = new Expense("id-1", "Dinner", new BigDecimal("30.00"), "Food", LocalDate.of(2026, 7, 31));
        repository.save(updated);

        assertThat(repository.findAll()).hasSize(1);
        Optional<Expense> found = repository.findById("id-1");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Dinner");
        assertThat(found.get().getAmount()).isEqualTo(new BigDecimal("30.00"));
    }

    @Test
    void findByCategory_shouldReturnMatchingExpensesCaseInsensitive() {
        repository.save(new Expense("1", "Lunch", new BigDecimal("15.50"), "Food", LocalDate.now()));
        repository.save(new Expense("2", "Bus", new BigDecimal("2.50"), "Transport", LocalDate.now()));
        repository.save(new Expense("3", "Coffee", new BigDecimal("4.50"), "food", LocalDate.now()));

        List<Expense> foodExpenses = repository.findByCategory("FOOD");

        assertThat(foodExpenses).hasSize(2)
                .extracting(Expense::getTitle)
                .containsExactlyInAnyOrder("Lunch", "Coffee");
    }

    @Test
    void deleteById_shouldRemoveExpenseWhenExists() {
        Expense expense = new Expense("1", "Lunch", new BigDecimal("15.50"), "Food", LocalDate.now());
        repository.save(expense);

        boolean deleted = repository.deleteById("1");

        assertThat(deleted).isTrue();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void deleteById_shouldReturnFalseWhenIdDoesNotExist() {
        boolean deleted = repository.deleteById("non-existent");

        assertThat(deleted).isFalse();
    }
}
