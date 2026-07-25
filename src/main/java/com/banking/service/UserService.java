package com.banking.service;

import com.banking.dto.request.ChangePasswordRequest;
import com.banking.dto.request.CreateCustomerRequest;
import com.banking.dto.request.RegisterRequest;
import com.banking.dto.request.UpdateProfileRequest;
import com.banking.dto.response.UserResponse;
import com.banking.entity.User;

import java.util.UUID;

/**
 * Service interface for user account management operations.
 */
public interface UserService {

    /**
     * Registers a new customer via the self-registration form.
     *
     * @param request the registration data
     * @return the created user response
     */
    UserResponse registerCustomer(RegisterRequest request);

    /**
     * Finds a user by their ID.
     *
     * @param userId the UUID of the user
     * @return the user entity
     */
    User findById(UUID userId);

    /**
     * Finds a user by their email address.
     *
     * @param email the email address
     * @return the user entity
     */
    User findByEmail(String email);

    /**
     * Changes the authenticated user's password after verifying the current one.
     *
     * @param userId  the UUID of the user
     * @param request the change password data
     */
    void changePassword(UUID userId, ChangePasswordRequest request);

    /**
     * Enables or disables a user account (admin operation).
     *
     * @param userId  the UUID of the user
     * @param enabled the new enabled state
     */
    void setEnabled(UUID userId, boolean enabled);

    /**
     * Unlocks a locked user account (admin operation).
     *
     * @param email the email of the user to unlock
     */
    void unlockAccount(String email);
}
