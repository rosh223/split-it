# AI Usage Notes & Architectural Decisions

This document provides an honest, detailed breakdown of how AI tools (including LLM assistants) were utilized during the development of the **Smart Expense Tracker API**, what was manually validated or refactored, and which AI suggestions were deliberately rejected to ensure a clean, production-ready, and evaluator-compatible codebase.

---

## 1. Which parts of the code were AI-generated vs. written by me

### AI-Generated Components (with human guidance & specification)
- **Boilerplate Spring Boot & DTO Classes**: Initial scaffolding for `Expense.java`, `ExpenseCreateRequest.java`, and `ExpenseSummary.java` was generated using AI assistants to quickly generate getters, setters, constructors, and Jakarta validation annotations (`@NotBlank`, `@Positive`, `@NotNull`).
- **OpenAPI / Swagger Annotations**: AI was used to draft descriptive Swagger annotations (`@Operation`, `@Tag`, `@Parameter`) on `ExpenseController.java` to make the interactive API documentation at `/swagger-ui.html` self-explanatory.
- **Initial MockMvc Test Shells**: The structure of `ExpenseControllerTest.java`, including basic `MockMvcRequestBuilders` boilerplate, was drafted with AI assistance.

### Written / Refactored Manually by Human Developer
- **Evaluator-Compatible Maven Directory Mapping**: Standard Maven/Spring Boot projects assume `src/main/java` and `src/test/java`. However, the automated grading script requires an exact root directory structure of `src/` and `tests/`. I manually designed and configured the custom source directory mappings in `pom.xml`:
  ```xml
  <sourceDirectory>src</sourceDirectory>
  <testSourceDirectory>tests</testSourceDirectory>
  ```
  This custom architectural solution allowed standard Spring Boot and `./mvnw test` to work seamlessly while strictly fulfilling the automated evaluator's folder structure rules.
- **Thread-Safe Concurrent JSON Storage (`ExpenseRepository.java`)**: Instead of naive file read/write logic suggested by AI, I manually implemented `ReentrantReadWriteLock` in `ExpenseRepository.java` to guarantee atomicity and prevent race conditions or file corruption when multiple concurrent requests read or modify `data/expenses.json`.
- **Jackson `LocalDate` Serialization Fixes**: Manually configured `ObjectMapper` with `JavaTimeModule` and disabled `WRITE_DATES_AS_TIMESTAMPS` so that `LocalDate` fields cleanly serialize to/from ISO `YYYY-MM-DD` strings instead of Jackson's default date-array format.
- **Bonus Feature (Monthly Aggregation Logic)**: Designed and refined `calculateMonthlySummary()` in `ExpenseService.java` using Java Streams and `DateTimeFormatter.ofPattern("yyyy-MM")` to provide accurate monthly expense totals and category breakdowns sorted chronologically.
- **Test Isolation Architecture**: Manually configured `@TestPropertySource(properties = {"storage.file.path=target/test-data/test-expenses.json"})` and `@TempDir` in unit tests so that running automated test suites never pollutes or deletes production data in `data/expenses.json`.

---

## 2. What you validated, tested, or changed in the AI's output, and why

1. **Validation of File Atomicity & Race Conditions**:
   - *What was tested*: Evaluated AI-generated file I/O methods under multiple read/write operations.
   - *Why changed*: The initial AI suggestion read and wrote to `data/expenses.json` without synchronization locks. I introduced `ReentrantReadWriteLock` around all read and write methods to ensure thread safety in web server environments.
2. **Correction of Jackson Date Format Handling**:
   - *What was tested*: Tested `POST` and `GET` requests with `date: "2026-07-31"`.
   - *Why changed*: AI-generated default `ObjectMapper` code failed to register `jsr310` Java time modules, causing `LocalDate` serialization exceptions. I explicitly instantiated and registered `JavaTimeModule()` and disabled date timestamps.
3. **HTTP Status Code Standardization**:
   - *What was tested*: Verified status codes against RESTful best practices.
   - *Why changed*: AI initially suggested returning `200 OK` for expense deletion and creation. I modified `POST /api/v1/expenses` to return `201 Created` and `DELETE /api/v1/expenses/{id}` to return `204 No Content` on success and `404 Not Found` when attempting to delete a non-existent ID.
4. **Comprehensive Edge-Case Testing**:
   - *What was tested*: Added test cases in `ExpenseControllerTest.java` to verify negative amounts, empty titles, case-insensitive category filtering (`"FOOD"` vs `"Food"`), and non-existent IDs.

---

## 3. Any AI suggestion you decided not to use, and why

1. **Rejected: JPA / Hibernate / H2 In-Memory Database**:
   - *AI Suggestion*: The AI strongly recommended using `spring-boot-starter-data-jpa` with an H2 or SQLite embedded database for persistence.
   - *Reason for Rejection*: The assignment brief explicitly noted *"Data can be stored in memory or a local JSON file; no database is required."* Introducing JPA/H2 would add unnecessary dependency bloat and violate the simplicity requested. A custom, thread-safe JSON file repository (`data/expenses.json`) was implemented instead.
2. **Rejected: Standard Maven Directory Layout (`src/main/java` & `src/test/java`)**:
   - *AI Suggestion*: The AI attempted to generate code inside standard Maven directories (`src/main/java/com/expensetracker/...` and `src/test/java/...`).
   - *Reason for Rejection*: The assignment grading specification explicitly states that submissions must follow the `your-repo/src/` and `your-repo/tests/` structure for automated review. Using standard Maven folders would cause the automated evaluator to fail to locate source code or tests.
3. **Rejected: Global Static In-Memory List Without Disk Persistence**:
   - *AI Suggestion*: For simplicity, an AI prompt suggested storing expenses purely in a static `CopyOnWriteArrayList` in memory.
   - *Reason for Rejection*: While allowed, purely in-memory data disappears when the application restarts. Storing data in a local JSON file (`data/expenses.json`) provides real-world persistence while remaining lightweight and easy to inspect.
