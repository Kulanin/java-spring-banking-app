# Enterprise Banking Simulator

A robust, concurrent backend banking simulator built with **Java** and **Spring Boot**, designed to enforce strict financial consistency, thread safety, idempotent transaction processing, and compliance-ready audit trails.

---

## 🚀 Key Engineering Highlights

* **Strict ACID Compliance & Transactional Boundaries:** Uses Spring's declarative transaction management (`@Transactional`) to guarantee data integrity across account creation, deposits, withdrawals, and peer-to-peer transfers.
* **Deadlock Prevention Strategy:** Implements smart resource ordering (`Math.min` / `Math.max`) during multi-account transfers to prevent circular-wait thread deadlocks under high concurrency.
* **Idempotency Protection:** Enforces unique idempotency key lookups (including derivative `-OUT` and `-IN` tracking) via `TransactionRecordService` to safely prevent double-spending or duplicate transactions during network retries.
* **Independent Audit Logging:** Automatically captures critical business actions (account creation, deposits, withdrawals, transfers, and duplicates) into a dedicated audit log using independent transaction propagation (`REQUIRES_NEW`), ensuring logs are safely persisted even if operations fail.
* **Modular Design & Separation of Concerns:** Leverages design patterns like the **Factory Pattern** (`AccountFactory`) for dynamic account creation and dedicated mappers (`TransactionMapper`) for clean DTO conversions.
* **Global Error Handling:** Centralized exception management (`@RestControllerAdvice`) mapping domain exceptions to secure, uniform responses.

---

## 🛠️ Tech Stack

* **Language:** Java 17+
* **Framework:** Spring Boot, Spring Data JPA
* **Database:** MySQL
* **Build Tool:** Maven

---

## 📦 Core Architecture & Services

### 1. `AccountService`
Handles core user accounts and single-account operations with strict transactional safety:
* **`createAccountForUser`**: Validates user existence, enforces unique account naming per user, utilizes `AccountFactory` for instantiation, and logs creation events.
* **`deposit` / `withdraw`**: Checks idempotency keys before processing, executes balance modifications within ACID boundaries, persists transaction records, and logs audit trails.

### 2. `TransferService`
Manages secure peer-to-peer fund movements with advanced concurrency controls:
* **`transfer`**: Enforces strict validation (e.g., preventing self-transfers), prevents deadlocks by sorting account execution IDs, updates balances atomically, and generates twin transaction legs (`-OUT` and `-IN`) with comprehensive audit tracking.

### 3. `AccountController` (REST API Layer)
Exposes secure RESTful endpoints adhering to modern API design standards:
* **Header-Based Idempotency:** Captures `Idempotency-Key` headers on state-changing requests (deposits, withdrawals, transfers) and gracefully returns `HTTP 409 Conflict` for duplicate requests.
* **Input Validation:** Enforces strict payload and path-variable validation using Jakarta Bean Validation (`@Valid`, `@Positive`, `@NotBlank`).
* **Standardized Envelope Responses:** Wraps all successes and errors in a consistent `ApiResponse` structure.

---

## ⚙️ Getting Started

### Prerequisites
* Java JDK 17 or higher
* MySQL Server running locally or via Docker

### Configuration
Update your `src/main/resources/application.yml` (or `application.properties`) with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database_name?useSSL=false&serverTimezone=UTC
    username: your_db_username
    password: your_db_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
