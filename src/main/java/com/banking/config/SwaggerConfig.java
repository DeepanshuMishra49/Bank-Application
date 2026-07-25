package com.banking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration for the Banking Management System REST API.
 *
 * <p>Access Swagger UI at: {@code /swagger-ui.html}
 * <p>Access raw OpenAPI JSON at: {@code /api-docs}
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures the OpenAPI specification with metadata, server info, and security schemes.
     *
     * @return a fully configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Management System API")
                        .description("""
                                **Nexus Bank** — Production-grade Banking Management System REST API.
                                
                                ## Features
                                - Customer management (CRUD, KYC, approval)
                                - Account management (open, close, freeze, activate)
                                - Transaction operations (deposit, withdrawal, transfer)
                                - Admin dashboard and analytics
                                - Employee KYC verification workflows
                                
                                ## Authentication
                                This API uses **session-based authentication**. Login via the Thymeleaf UI
                                at `/login` before using these endpoints. For API testing, send the session
                                cookie with each request.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Nexus Bank API Team")
                                .email("api@nexusbank.com")
                                .url("https://nexusbank.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://nexusbank.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.nexusbank.com").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Session cookie obtained after successful login")));
    }
}
