package com.banking.service.impl;

import com.banking.dto.request.CreateCustomerRequest;
import com.banking.dto.response.CustomerResponse;
import com.banking.entity.Employee;
import com.banking.entity.Role;
import com.banking.entity.User;
import com.banking.enums.RoleName;
import com.banking.exception.DuplicateUserException;
import com.banking.repository.CustomerRepository;
import com.banking.repository.EmployeeRepository;
import com.banking.repository.RoleRepository;
import com.banking.repository.UserRepository;
import com.banking.service.CustomerService;
import com.banking.service.EmployeeService;
import com.banking.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Implementation of {@link EmployeeService} managing employee accounts and pending customers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountNumberGenerator accountNumberGenerator;
    private final CustomerService customerService;

    @Override
    public CustomerResponse createEmployee(CreateCustomerRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already registered: " + request.email());
        }

        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("ROLE_EMPLOYEE not found"));

        String baseUsername = request.email().split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = ensureUniqueUsername(baseUsername);

        User user = User.builder()
                .username(username)
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of(employeeRole))
                .build();
        user = userRepository.save(user);

        long count = employeeRepository.count() + 1;
        Employee employee = Employee.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .employeeId(accountNumberGenerator.generateEmployeeId(count))
                .designation("Banking Officer")
                .department("Operations")
                .active(true)
                .build();
        employeeRepository.save(employee);

        log.info("Employee created: {} ({})", employee.getFullName(), employee.getEmployeeId());

        // Return as CustomerResponse (reuse for simplicity in admin UI)
        return new CustomerResponse(
                user.getId(), employee.getEmployeeId(),
                employee.getFirstName(), employee.getLastName(), employee.getFullName(),
                user.getEmail(), user.getPhone(),
                null, null, "Banking Officer", null,
                true, null, null, null, null, null, null,
                false, 0, user.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getPendingCustomers(Pageable pageable) {
        return customerService.getPendingApprovals(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(e -> new CustomerResponse(
                e.getId(), e.getEmployeeId(),
                e.getFirstName(), e.getLastName(), e.getFullName(),
                e.getUser().getEmail(), e.getUser().getPhone(),
                null, null, e.getDesignation(), null,
                e.isActive(), null,
                null, null, null, null, null,
                false, 0, e.getCreatedAt()
        ));
    }

    private String ensureUniqueUsername(String base) {
        String username = base;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + suffix++;
        }
        return username;
    }
}
