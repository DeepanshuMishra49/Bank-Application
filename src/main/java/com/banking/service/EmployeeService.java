package com.banking.service;

import com.banking.dto.request.CreateCustomerRequest;
import com.banking.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for employee profile management.
 */
public interface EmployeeService {

    /**
     * Creates a new employee user account.
     *
     * @param request the employee registration data
     * @return the created employee details
     */
    CustomerResponse createEmployee(CreateCustomerRequest request);

    /**
     * Retrieves all pending customers awaiting KYC or account approval.
     *
     * @param pageable pagination parameters
     * @return paginated list of pending customers
     */
    Page<CustomerResponse> getPendingCustomers(Pageable pageable);

    /**
     * Retrieves all employees.
     *
     * @param pageable pagination parameters
     * @return paginated list of all employees as CustomerResponse
     */
    Page<CustomerResponse> getAllEmployees(Pageable pageable);
}
