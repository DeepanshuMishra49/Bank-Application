package com.banking.security;

import com.banking.entity.User;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads user details from the database for Spring Security authentication.
 * Supports login by either username or email.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their username or email address.
     *
     * @param usernameOrEmail the username or email to look up
     * @return a {@link UserDetails} object for authentication
     * @throws UsernameNotFoundException if no user is found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    log.warn("Authentication attempt with unknown identifier: {}", usernameOrEmail);
                    return new UsernameNotFoundException(
                            "No user found with username or email: " + usernameOrEmail);
                });

        log.debug("Loaded user for authentication: {}", user.getUsername());
        return new CustomUserDetails(user);
    }
}
