package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class ExpenseRepository {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseRepository.class);

    private final String storageFilePath;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ExpenseRepository(@Value("${storage.file.path:data/expenses.json}") String storageFilePath) {
        this.storageFilePath = storageFilePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    public void init() {
        lock.writeLock().lock();
        try {
            Path path = Paths.get(storageFilePath);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                writeExpensesToFile(new ArrayList<>());
                logger.info("Initialized storage file at: {}", storageFilePath);
            }
        } catch (IOException e) {
            logger.error("Failed to initialize expense storage file: {}", storageFilePath, e);
            throw new RuntimeException("Could not initialize storage file", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Expense save(Expense expense) {
        lock.writeLock().lock();
        try {
            List<Expense> expenses = readExpensesFromFile();
            // Replace if existing ID, else append
            boolean updated = false;
            for (int i = 0; i < expenses.size(); i++) {
                if (expenses.get(i).getId().equals(expense.getId())) {
                    expenses.set(i, expense);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                expenses.add(expense);
            }
            writeExpensesToFile(expenses);
            return expense;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Expense> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(readExpensesFromFile());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Expense> findById(String id) {
        lock.readLock().lock();
        try {
            return readExpensesFromFile().stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Expense> findByCategory(String category) {
        lock.readLock().lock();
        try {
            return readExpensesFromFile().stream()
                    .filter(e -> e.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean deleteById(String id) {
        lock.writeLock().lock();
        try {
            List<Expense> expenses = readExpensesFromFile();
            boolean removed = expenses.removeIf(e -> e.getId().equals(id));
            if (removed) {
                writeExpensesToFile(expenses);
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clearAll() {
        lock.writeLock().lock();
        try {
            writeExpensesToFile(new ArrayList<>());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<Expense> readExpensesFromFile() {
        File file = new File(storageFilePath);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<List<Expense>>() {});
        } catch (IOException e) {
            logger.error("Failed to read expenses from file: {}", storageFilePath, e);
            throw new RuntimeException("Could not read from storage file", e);
        }
    }

    private void writeExpensesToFile(List<Expense> expenses) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(storageFilePath), expenses);
        } catch (IOException e) {
            logger.error("Failed to write expenses to file: {}", storageFilePath, e);
            throw new RuntimeException("Could not write to storage file", e);
        }
    }
}
