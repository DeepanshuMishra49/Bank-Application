# 🏦 Enterprise Banking Management System

An enterprise-grade, secure, multi-role digital banking portal built with **Spring Boot 3.3.4**, **Java 21**, **Spring Security 6**, **Thymeleaf**, and **PostgreSQL**.

---

## 🌟 Key Features

### 🔒 Enterprise Security & Auth
- **Role-Based Access Control (RBAC)**: Distinct permissions for `ROLE_ADMIN`, `ROLE_EMPLOYEE`, and `ROLE_CUSTOMER`.
- **Password Hashing**: Production-grade **BCrypt** with strength 12.
- **Brute Force Protection**: Automatic account locking for 15 minutes after 5 consecutive failed attempts.
- **CSRF & Security Headers**: Full CSRF protection, CSP, frame options, and HSTS headers.
- **Remember-Me & Session Control**: Cookie-based persistent sessions and single session enforcement.

### 👤 Customer Self-Service Portal
- **Dashboard**: Real-time balance summary, active accounts, transaction counts, and quick actions.
- **Account Management**: View savings & checking accounts, IFSC codes, daily transfer limits, and minimum balance tracking.
- **Fund Transfer**: Instant inter-account transfers with balance validation, self-transfer prevention, and daily limit enforcement.
- **Cash Deposit & Withdrawal**: Real-time balance updates with minimum balance rules (`₹500`).
- **Transaction History & Mini-Statement**: Filterable transaction log and instant 10-record mini statements with PDF export capability.
- **Profile & Password**: Self-profile updates and secure password change forms.

### 👔 Employee Operations
- **Pending Approvals Queue**: One-click approval of new customer registrations.
- **KYC Verification Workflow**: Review document details with instant approval or multi-reason rejection modal.
- **Customer & Account Auditing**: Inspect customer accounts, status tracking, and account freeze/unfreeze controls.

### 👑 Admin Control Panel
- **Executive Analytics**: System-wide dashboard with 8 real-time KPI metrics, total assets, daily deposits, and transaction activity charts.
- **Customer Management**: Search, create, approve, and deactivate customer accounts.
- **Account Controls**: Search accounts, override statuses (Active, Frozen, Closed), and modify limits.
- **Global Transaction Ledger**: Complete system-wide audit of all money movements.
- **Employee Management**: Manage bank staff profiles and roles.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Framework** | Spring Boot 3.3.4 |
| **Language** | Java 21 |
| **Persistence** | Spring Data JPA, Hibernate, PostgreSQL 16 |
| **Database Migrations** | Flyway (`V1__init_schema.sql`, `V2__seed_data.sql`) |
| **Security** | Spring Security 6, BCrypt, Custom Handlers |
| **Frontend Rendering** | Thymeleaf 3, Thymeleaf Security Extras, Thymeleaf Layout Dialect |
| **Styling & Assets** | Vanilla CSS3 (Glassmorphic dark theme, responsive flex/grid), Bootstrap Icons |
| **Client Scripting** | Modern ES6+ JavaScript, Chart.js 4.4 |
| **Containerization** | Docker, Docker Compose (Multi-stage build) |

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21 JDK**
- **Maven 3.8+** (or use included `./mvnw`)
- **Docker & Docker Compose** (optional, for containerized run)
- **PostgreSQL 16** (if running locally without Docker)

---

### Option 1: Running with Docker Compose (Recommended)

1. Clone or navigate to the project directory:
   ```bash
   cd Banking-Application
   ```

2. Start the database and application:
   ```bash
   docker-compose up --build -d
   ```

3. Access the application:
   - **Web Portal**: [http://localhost:8080](http://localhost:8080)
   - **Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
   - **Swagger API Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

### Option 2: Running Locally

1. Start PostgreSQL on localhost:5432 with database `banking_db`, user `banking_user`, password `banking_password`.

2. Build and run the application:
   ```bash
   ./mvnw clean spring-boot:run
   ```

3. Open [http://localhost:8080](http://localhost:8080) in your browser.

---

## 🔑 Default Credentials

Seed data is automatically applied via Flyway (`V2__seed_data.sql`):

| Role | Username | Password | Access Path |
|---|---|---|---|
| **Admin** | `admin` | `Admin@123` | `/admin/dashboard` |
| **Employee** | `emp001` | `Employee@123` | `/employee/dashboard` |
| **Customer** | *(Self-register)* | *(Your choice)* | `/customer/dashboard` |

---

## 📁 Project Architecture

```
Banking-Application/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── src/
    └── main/
        ├── java/com/banking/
        │   ├── config/              # SecurityConfig, WebConfig, OpenApiConfig, AuditConfig
        │   ├── controller/
        │   │   ├── api/             # REST controllers for Swagger/mobile APIs
        │   │   └── web/             # AuthController, CustomerController, AdminController, EmployeeController
        │   ├── dto/                 # Request & Response records
        │   ├── entity/              # User, Customer, Account, Transaction, KycDetail, Employee, etc.
        │   ├── enums/               # AccountType, AccountStatus, TransactionType, RoleName, etc.
        │   ├── exception/           # Custom banking domain exceptions & GlobalExceptionHandler
        │   ├── repository/          # JPA Repositories
        │   ├── security/            # UserDetails, Success/Failure Handlers, AccessDenied
        │   ├── service/             # Service interfaces & implementations
        │   └── util/                # AccountNumberGenerator, BankingConstants
        └── resources/
            ├── application.yml
            ├── logback-spring.xml
            ├── db/migration/        # Flyway schema (V1) & seed data (V2)
            ├── static/
            │   ├── css/style.css    # Comprehensive dark glassmorphism stylesheet
            │   └── js/app.js        # Theme, toasts, modals, Chart.js integration
            └── templates/           # 25 Thymeleaf HTML templates across layout, auth, customer, admin, employee, error
```

---

## 📊 Database ER Diagram Overview

- **Users ↔ Roles**: Many-to-Many via `user_roles`
- **User ↔ Customer**: One-to-One
- **User ↔ Employee**: One-to-One
- **Customer ↔ Accounts**: One-to-Many
- **Account ↔ Transactions**: One-to-Many
- **Customer ↔ KycDetail**: One-to-One

---

## 📄 License
This project is licensed under the MIT License.
