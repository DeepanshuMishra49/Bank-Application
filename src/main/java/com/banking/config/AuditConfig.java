package com.banking.config;

import com.banking.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA auditing configuration providing the current auditor (username)
 * for {@code @CreatedBy} and {@code @LastModifiedBy} fields.
 */
@Configuration
@Slf4j
public class AuditConfig {

    /**
     * Returns the username of the currently authenticated user for auditing purposes.
     *
     * @return AuditorAware resolving to the current username
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("SYSTEM");
            }
            if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                return Optional.of(userDetails.getUsername());
            }
            return Optional.of(authentication.getName());
        };
    }
}
