package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.model.ExpenseCreateRequest;
import com.expensetracker.repository.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "storage.file.path=target/test-data/test-expenses.json"
})
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository repository;

    @BeforeEach
    void setUp() {
        repository.clearAll();
    }

    @AfterEach
    void tearDown() {
        repository.clearAll();
    }

    @Test
    void addExpense_shouldReturn201_whenRequestIsValid() throws Exception {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "Groceries",
                new BigDecimal("45.50"),
                "Food",
                LocalDate.of(2026, 7, 31)
        );

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Groceries")))
                .andExpect(jsonPath("$.amount", is(45.50)))
                .andExpect(jsonPath("$.category", is("Food")))
                .andExpect(jsonPath("$.date", is("2026-07-31")));
    }

    @Test
    void addExpense_shouldReturn400_whenAmountIsNegative() throws Exception {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "Groceries",
                new BigDecimal("-10.00"),
                "Food",
                LocalDate.of(2026, 7, 31)
        );

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.details.amount", notNullValue()));
    }

    @Test
    void getExpenses_shouldReturnAllExpenses_andFilterByCategory() throws Exception {
        repository.save(new Expense("id-1", "Lunch", new BigDecimal("15.00"), "Food", LocalDate.of(2026, 7, 31)));
        repository.save(new Expense("id-2", "Taxi", new BigDecimal("25.00"), "Transport", LocalDate.of(2026, 7, 31)));

        // Get all expenses
        mockMvc.perform(get("/api/v1/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Filter by Food
        mockMvc.perform(get("/api/v1/expenses").param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Lunch")));
    }

    @Test
    void getSummary_shouldCalculateOverallTotalAndCategoryBreakdown() throws Exception {
        repository.save(new Expense("id-1", "Lunch", new BigDecimal("15.00"), "Food", LocalDate.of(2026, 7, 31)));
        repository.save(new Expense("id-2", "Dinner", new BigDecimal("35.00"), "Food", LocalDate.of(2026, 7, 31)));
        repository.save(new Expense("id-3", "Taxi", new BigDecimal("25.00"), "Transport", LocalDate.of(2026, 7, 31)));

        mockMvc.perform(get("/api/v1/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount", is(75.00)))
                .andExpect(jsonPath("$.categoryBreakdown.Food", is(50.00)))
                .andExpect(jsonPath("$.categoryBreakdown.Transport", is(25.00)));
    }

    @Test
    void getMonthlySummary_shouldGroupExpensesByMonth() throws Exception {
        repository.save(new Expense("id-1", "July Expense", new BigDecimal("100.00"), "Food", LocalDate.of(2026, 7, 15)));
        repository.save(new Expense("id-2", "June Expense", new BigDecimal("50.00"), "Transport", LocalDate.of(2026, 6, 10)));

        mockMvc.perform(get("/api/v1/expenses/summary/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].month", is("2026-07")))
                .andExpect(jsonPath("$[0].totalAmount", is(100.00)))
                .andExpect(jsonPath("$[0].expenseCount", is(1)))
                .andExpect(jsonPath("$[1].month", is("2026-06")))
                .andExpect(jsonPath("$[1].totalAmount", is(50.00)));
    }

    @Test
    void getExpenseById_shouldReturn200WhenFound_and404WhenNotFound() throws Exception {
        repository.save(new Expense("id-1", "Lunch", new BigDecimal("15.00"), "Food", LocalDate.of(2026, 7, 31)));

        mockMvc.perform(get("/api/v1/expenses/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Lunch")));

        mockMvc.perform(get("/api/v1/expenses/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Expense not found")));
    }

    @Test
    void deleteExpense_shouldReturn204WhenDeleted_and404WhenNotExists() throws Exception {
        repository.save(new Expense("id-1", "Lunch", new BigDecimal("15.00"), "Food", LocalDate.of(2026, 7, 31)));

        mockMvc.perform(delete("/api/v1/expenses/id-1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/expenses/id-1"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/expenses/id-1"))
                .andExpect(status().isNotFound());
    }
}
