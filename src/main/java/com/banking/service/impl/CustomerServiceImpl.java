package com.banking.service.impl;

import com.banking.dto.request.CreateCustomerRequest;
import com.banking.dto.request.UpdateProfileRequest;
import com.banking.dto.response.CustomerResponse;
import com.banking.entity.*;
import com.banking.enums.RoleName;
import com.banking.exception.DuplicateUserException;
import com.banking.exception.UserNotFoundException;
import com.banking.repository.*;
import com.banking.service.CustomerService;
import com.banking.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Implementation of {@link CustomerService} for customer profile management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KycRepository kycRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findByUserId(UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("userId", userId.toString()));
        return toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findByCustomerId(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new UserNotFoundException("customerId", customerId));
        return toResponse(customer);
    }

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already registered: " + request.email());
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicateUserException("Phone already registered: " + request.phone());
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));

        String baseUsername = request.email().split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = ensureUniqueUsername(baseUsername);

        User user = User.builder()
                .username(username)
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of(customerRole))
                .build();
        user = userRepository.save(user);

        Address address = null;
        if (request.street() != null) {
            address = Address.builder()
                    .street(request.street())
                    .city(request.city() != null ? request.city() : "")
                    .state(request.state() != null ? request.state() : "")
                    .pinCode(request.pinCode() != null ? request.pinCode() : "")
                    .build();
        }

        long count = customerRepository.count() + 1;
        Customer customer = Customer.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .gender(request.gender())
                .occupation(request.occupation())
                .annualIncome(request.annualIncome())
                .customerId(accountNumberGenerator.generateCustomerId(count))
                .approved(false)
                .address(address)
                .build();

        customer = customerRepository.save(customer);
        log.info("Customer created by admin: {} ({})", customer.getFullName(), customer.getCustomerId());
        return toResponse(customer);
    }

    @Override
    public CustomerResponse updateProfile(UUID customerId, UpdateProfileRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UserNotFoundException("id", customerId.toString()));

        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setGender(request.gender());
        customer.setOccupation(request.occupation());
        customer.setAnnualIncome(request.annualIncome());

        // Update phone on user
        if (request.phone() != null && !request.phone().equals(customer.getUser().getPhone())) {
            if (userRepository.existsByPhone(request.phone())) {
                throw new DuplicateUserException("Phone already in use: " + request.phone());
            }
            customer.getUser().setPhone(request.phone());
            userRepository.save(customer.getUser());
        }

        if (request.street() != null) {
            Address address = customer.getAddress();
            if (address == null) {
                address = new Address();
            }
            address.setStreet(request.street());
            address.setCity(request.city() != null ? request.city() : "");
            address.setState(request.state() != null ? request.state() : "");
            address.setPinCode(request.pinCode() != null ? request.pinCode() : "");
            customer.setAddress(address);
        }

        customer = customerRepository.save(customer);
        log.info("Profile updated for customer: {}", customer.getCustomerId());
        return toResponse(customer);
    }

    @Override
    public CustomerResponse approveCustomer(String customerId, String approvedBy) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new UserNotFoundException("customerId", customerId));
        customer.setApproved(true);
        customer.setApprovedBy(approvedBy);
        customer = customerRepository.save(customer);
        log.info("Customer {} approved by {}", customerId, approvedBy);
        return toResponse(customer);
    }

    @Override
    public void deleteCustomer(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new UserNotFoundException("customerId", customerId));
        customer.getUser().setEnabled(false);
        userRepository.save(customer.getUser());
        log.info("Customer {} deactivated", customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String search, Pageable pageable) {
        return customerRepository.searchCustomers(search, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getPendingApprovals(Pageable pageable) {
        return customerRepository.findByApproved(false, pageable).map(this::toResponse);
    }

    private CustomerResponse toResponse(Customer c) {
        boolean kycVerified = c.getKycDetail() != null && c.getKycDetail().isVerified();
        Address addr = c.getAddress();
        return new CustomerResponse(
                c.getId(),
                c.getCustomerId(),
                c.getFirstName(),
                c.getLastName(),
                c.getFullName(),
                c.getUser().getEmail(),
                c.getUser().getPhone(),
                c.getDateOfBirth(),
                c.getGender(),
                c.getOccupation(),
                c.getAnnualIncome(),
                c.isApproved(),
                c.getApprovedBy(),
                addr != null ? addr.getStreet() : null,
                addr != null ? addr.getCity() : null,
                addr != null ? addr.getState() : null,
                addr != null ? addr.getPinCode() : null,
                addr != null ? addr.getCountry() : null,
                kycVerified,
                c.getAccounts().size(),
                c.getCreatedAt()
        );
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
