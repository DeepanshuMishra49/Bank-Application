package com.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a saved beneficiary/payee.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BeneficiaryResponse(
        UUID id,
        String name,
        String accountNumber,
        String bankName,
        String ifscCode,
        String nickname,
        boolean active,
        LocalDateTime createdAt
) {}
