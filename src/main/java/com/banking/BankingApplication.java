package com.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Banking Management System application.
 *
 * <p>This application provides a production-grade online banking portal with:
 * <ul>
 *   <li>Role-based access control (ADMIN, EMPLOYEE, CUSTOMER)</li>
 *   <li>Session-based authentication with brute-force protection</li>
 *   <li>Full banking operations: deposits, withdrawals, transfers</li>
 *   <li>Thymeleaf-based responsive UI with glassmorphism design</li>
 *   <li>REST APIs documented via Swagger/OpenAPI</li>
 * </ul>
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableScheduling
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }
}
