package com.expensetracker.exception;

public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(String id) {
        super("Expense not found with ID: " + id);
    }
}
