package com.banking.repository;

import com.banking.entity.KycDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link KycDetail} entity.
 */
@Repository
public interface KycRepository extends JpaRepository<KycDetail, UUID> {

    Optional<KycDetail> findByCustomerId(UUID customerId);

    long countByVerified(boolean verified);

    /**
     * Returns paginated KYC entries filtered by verification status.
     */
    Page<KycDetail> findByVerified(boolean verified, Pageable pageable);
}
