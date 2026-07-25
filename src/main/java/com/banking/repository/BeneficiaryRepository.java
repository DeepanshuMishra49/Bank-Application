package com.banking.repository;

import com.banking.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Beneficiary} entity.
 */
@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByCustomerIdAndActiveTrue(UUID customerId);

    boolean existsByCustomerIdAndAccountNumber(UUID customerId, String accountNumber);
}
