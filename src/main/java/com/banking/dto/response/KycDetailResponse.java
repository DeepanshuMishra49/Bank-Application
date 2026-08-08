package com.banking.dto.response;

import com.banking.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for KYC document details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record KycDetailResponse(
        UUID id,
        String customerId,
        String customerName,
        String customerEmail,
        DocumentType documentType,
        String documentNumber,
        String documentFrontUrl,
        String documentBackUrl,
        boolean verified,
        String verifiedBy,
        LocalDateTime verifiedAt,
        String rejectionReason,
        LocalDateTime submittedAt
) {}
