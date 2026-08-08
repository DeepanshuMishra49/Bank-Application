package com.banking.service.impl;

import com.banking.dto.request.KycVerificationRequest;
import com.banking.dto.response.KycDetailResponse;
import com.banking.entity.Customer;
import com.banking.entity.KycDetail;
import com.banking.exception.UserNotFoundException;
import com.banking.repository.CustomerRepository;
import com.banking.repository.KycRepository;
import com.banking.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of {@link KycService} managing the KYC verification lifecycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KycServiceImpl implements KycService {

    private final KycRepository kycRepository;
    private final CustomerRepository customerRepository;

    @Override
    public KycDetail submitKyc(KycVerificationRequest request) {
        Customer customer = customerRepository.findByCustomerId(request.customerId())
                .orElseThrow(() -> new UserNotFoundException("customerId", request.customerId()));

        KycDetail kyc = kycRepository.findByCustomerId(customer.getId())
                .orElse(KycDetail.builder().customer(customer).build());

        kyc.setDocumentType(request.documentType());
        kyc.setDocumentNumber(request.documentNumber());
        kyc.setVerified(false);
        kyc.setRejectionReason(null);

        kyc = kycRepository.save(kyc);
        log.info("KYC submitted for customer: {}", request.customerId());
        return kyc;
    }

    @Override
    public KycDetail verifyKyc(String customerId, String verifiedBy) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new UserNotFoundException("customerId", customerId));

        KycDetail kyc = kycRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> KycDetail.builder()
                        .customer(customer)
                        .documentType(com.banking.enums.DocumentType.AADHAAR_CARD)
                        .documentNumber("DOC-" + customerId)
                        .build());

        kyc.setVerified(true);
        kyc.setVerifiedBy(verifiedBy);
        kyc.setVerifiedAt(LocalDateTime.now());
        kyc.setRejectionReason(null);
        kyc = kycRepository.save(kyc);

        log.info("KYC verified for customer: {} by {}", customerId, verifiedBy);
        return kyc;
    }

    @Override
    public KycDetail rejectKyc(String customerId, String rejectionReason, String rejectedBy) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new UserNotFoundException("customerId", customerId));

        KycDetail kyc = kycRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> KycDetail.builder()
                        .customer(customer)
                        .documentType(com.banking.enums.DocumentType.AADHAAR_CARD)
                        .documentNumber("DOC-" + customerId)
                        .build());

        kyc.setVerified(false);
        kyc.setRejectionReason(rejectionReason);
        kyc = kycRepository.save(kyc);

        log.info("KYC rejected for customer: {} by {} - Reason: {}", customerId, rejectedBy, rejectionReason);
        return kyc;
    }

    @Override
    @Transactional(readOnly = true)
    public KycDetail getKycByCustomerId(UUID customerId) {
        return kycRepository.findByCustomerId(customerId)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KycDetailResponse> getAllKyc(Pageable pageable) {
        return kycRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KycDetailResponse> getPendingKyc(Pageable pageable) {
        return kycRepository.findByVerified(false, pageable).map(this::toResponse);
    }

    // ─── Mapper ────────────────────────────────────────────────────────────────

    private KycDetailResponse toResponse(KycDetail kyc) {
        Customer c = kyc.getCustomer();
        String custId    = c != null ? c.getCustomerId() : null;
        String custName  = c != null ? c.getFullName()   : null;
        String custEmail = (c != null && c.getUser() != null) ? c.getUser().getEmail() : null;

        return new KycDetailResponse(
                kyc.getId(),
                custId,
                custName,
                custEmail,
                kyc.getDocumentType(),
                kyc.getDocumentNumber(),
                kyc.getDocumentFrontUrl(),
                kyc.getDocumentBackUrl(),
                kyc.isVerified(),
                kyc.getVerifiedBy(),
                kyc.getVerifiedAt(),
                kyc.getRejectionReason(),
                kyc.getCreatedAt()
        );
    }
}
