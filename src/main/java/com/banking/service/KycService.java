package com.banking.service;

import com.banking.dto.request.KycVerificationRequest;
import com.banking.dto.response.KycDetailResponse;
import com.banking.entity.KycDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for KYC document submission and verification workflow.
 */
public interface KycService {

    KycDetail submitKyc(KycVerificationRequest request);

    KycDetail verifyKyc(String customerId, String verifiedBy);

    KycDetail rejectKyc(String customerId, String rejectionReason, String rejectedBy);

    KycDetail getKycByCustomerId(UUID customerId);

    /**
     * Returns all KYC entries (all statuses) for admin view.
     */
    Page<KycDetailResponse> getAllKyc(Pageable pageable);

    /**
     * Returns only pending (unverified) KYC entries for admin/employee action.
     */
    Page<KycDetailResponse> getPendingKyc(Pageable pageable);
}

