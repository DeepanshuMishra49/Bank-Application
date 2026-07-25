package com.banking.service.impl;

import com.banking.dto.request.ChangePasswordRequest;
import com.banking.dto.request.RegisterRequest;
import com.banking.dto.response.UserResponse;
import com.banking.entity.Customer;
import com.banking.entity.Role;
import com.banking.entity.User;
import com.banking.enums.RoleName;
import com.banking.exception.DuplicateUserException;
import com.banking.exception.UserNotFoundException;
import com.banking.exception.ValidationException;
import com.banking.repository.CustomerRepository;
import com.banking.repository.RoleRepository;
import com.banking.repository.UserRepository;
import com.banking.service.UserService;
import com.banking.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Implementation of {@link UserService} providing user account operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    public UserResponse registerCustomer(RegisterRequest request) {
        // Validate uniqueness
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already registered: " + request.email());
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicateUserException("Phone number already registered: " + request.phone());
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found in database"));

        // Generate unique username from email prefix
        String baseUsername = request.email().split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = generateUniqueUsername(baseUsername);

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

        // Create customer profile
        long count = customerRepository.count() + 1;
        Customer customer = Customer.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .customerId(accountNumberGenerator.generateCustomerId(count))
                .approved(false)
                .build();
        customerRepository.save(customer);

        log.info("New customer registered: {} ({})", user.getEmail(), customer.getCustomerId());

        return toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("id", userId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ValidationException("New passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    public void setEnabled(UUID userId, boolean enabled) {
        User user = findById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("User {} status set to enabled={}", user.getEmail(), enabled);
    }

    @Override
    public void unlockAccount(String email) {
        userRepository.unlockAccount(email);
        log.info("Account unlocked for: {}", email);
    }

    private String generateUniqueUsername(String base) {
        String username = base;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + suffix++;
        }
        return username;
    }

    private UserResponse toUserResponse(User user) {
        Set<String> roles = new java.util.HashSet<>();
        user.getRoles().forEach(role -> roles.add(role.getName().name()));
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                roles,
                user.getProfilePictureUrl(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
