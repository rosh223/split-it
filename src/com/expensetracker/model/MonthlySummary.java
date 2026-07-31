package com.expensetracker.model;

import java.math.BigDecimal;
import java.util.Map;

public class MonthlySummary {

    private String month;
    private BigDecimal totalAmount;
    private long expenseCount;
    private Map<String, BigDecimal> categoryBreakdown;

    public MonthlySummary() {
    }

    public MonthlySummary(String month, BigDecimal totalAmount, long expenseCount, Map<String, BigDecimal> categoryBreakdown) {
        this.month = month;
        this.totalAmount = totalAmount;
        this.expenseCount = expenseCount;
        this.categoryBreakdown = categoryBreakdown;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(long expenseCount) {
        this.expenseCount = expenseCount;
    }

    public Map<String, BigDecimal> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<String, BigDecimal> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }
}
