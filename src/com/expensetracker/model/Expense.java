package com.expensetracker.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Expense {

    @NotBlank(message = "ID cannot be empty")
    private String id;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Category cannot be empty")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;

    public Expense() {
    }

    public Expense(String id, String title, BigDecimal amount, String category, LocalDate date) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id) &&
               Objects.equals(title, expense.title) &&
               Objects.equals(amount, expense.amount) &&
               Objects.equals(category, expense.category) &&
               Objects.equals(date, expense.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, amount, category, date);
    }

    @Override
    public String toString() {
        return "Expense{" +
               "id='" + id + '\'' +
               ", title='" + title + '\'' +
               ", amount=" + amount +
               ", category='" + category + '\'' +
               ", date=" + date +
               '}';
    }
}
