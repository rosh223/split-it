package com.expensetracker.model;

import java.math.BigDecimal;
import java.util.Map;

public class ExpenseSummary {

    private BigDecimal totalAmount;
    private Map<String, BigDecimal> categoryBreakdown;

    public ExpenseSummary() {
    }

    public ExpenseSummary(BigDecimal totalAmount, Map<String, BigDecimal> categoryBreakdown) {
        this.totalAmount = totalAmount;
        this.categoryBreakdown = categoryBreakdown;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Map<String, BigDecimal> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<String, BigDecimal> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }
}
