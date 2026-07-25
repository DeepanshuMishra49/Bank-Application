package com.banking.service;

import com.banking.dto.request.KycVerificationRequest;
import com.banking.entity.KycDetail;

import java.util.UUID;

/**
 * Service interface for KYC document submission and verification workflow.
 */
public interface KycService {

    KycDetail submitKyc(KycVerificationRequest request);

    KycDetail verifyKyc(String customerId, String verifiedBy);

    KycDetail rejectKyc(String customerId, String rejectionReason, String rejectedBy);

    KycDetail getKycByCustomerId(UUID customerId);
}
