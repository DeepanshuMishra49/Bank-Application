package com.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for user account information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        UUID id,
        String username,
        String email,
        String phone,
        boolean enabled,
        boolean accountNonLocked,
        Set<String> roles,
        String profilePictureUrl,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {}
