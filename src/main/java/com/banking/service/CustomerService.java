package com.banking.service;

import com.banking.dto.request.CreateCustomerRequest;
import com.banking.dto.request.KycVerificationRequest;
import com.banking.dto.request.UpdateProfileRequest;
import com.banking.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for customer profile management.
 */
public interface CustomerService {

    CustomerResponse findByUserId(UUID userId);

    CustomerResponse findByCustomerId(String customerId);

    CustomerResponse createCustomer(CreateCustomerRequest request);

    CustomerResponse updateProfile(UUID customerId, UpdateProfileRequest request);

    CustomerResponse approveCustomer(String customerId, String approvedBy);

    void deleteCustomer(String customerId);

    Page<CustomerResponse> getAllCustomers(Pageable pageable);

    Page<CustomerResponse> searchCustomers(String search, Pageable pageable);

    Page<CustomerResponse> getPendingApprovals(Pageable pageable);
}
