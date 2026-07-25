package com.banking.security;

import com.banking.repository.UserRepository;
import com.banking.util.BankingConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles failed login attempts by incrementing failure counters and
 * locking accounts after reaching the maximum allowed attempts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String usernameOrEmail = request.getParameter("username");
        String errorParam = "error";

        if (exception instanceof LockedException) {
            errorParam = "locked";
            log.warn("Login attempt on locked account: {}", usernameOrEmail);
        } else if (exception instanceof DisabledException) {
            errorParam = "disabled";
            log.warn("Login attempt on disabled account: {}", usernameOrEmail);
        } else if (exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException) {
            // Increment failed attempts
            userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).ifPresent(user -> {
                int newAttempts = user.getFailedAttempts() + 1;
                userRepository.updateFailedAttempts(newAttempts, user.getEmail());
                log.warn("Failed login attempt #{} for user: {}", newAttempts, user.getEmail());

                if (newAttempts >= BankingConstants.MAX_FAILED_ATTEMPTS) {
                    userRepository.lockAccount(user.getEmail());
                    log.warn("Account locked due to {} failed attempts: {}", newAttempts, user.getEmail());
                    errorParam = "locked";
                }
            });
        }

        String ip = request.getRemoteAddr();
        log.warn("Authentication failure from IP: {} - {}", ip, exception.getMessage());

        response.sendRedirect(request.getContextPath() + "/login?" + errorParam);
    }
}
