package com.banking.security;

import com.banking.entity.User;
import com.banking.repository.UserRepository;
import com.banking.util.BankingConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Handles successful authentication by resetting failed attempts, recording
 * login metadata, and redirecting to the appropriate role-based dashboard.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Reset failed attempts on successful login
        userRepository.unlockAccount(userDetails.getEmail());

        // Update last login info
        userRepository.findByEmail(userDetails.getEmail()).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(getClientIp(request));
            userRepository.save(user);
        });

        log.info("Successful login for user: {} from IP: {}", username, getClientIp(request));

        // Redirect based on role
        String redirectUrl = determineRedirectUrl(authentication.getAuthorities());
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    private String determineRedirectUrl(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            return switch (authority.getAuthority()) {
                case BankingConstants.ROLE_ADMIN -> BankingConstants.ADMIN_DASHBOARD_URL;
                case BankingConstants.ROLE_EMPLOYEE -> BankingConstants.EMPLOYEE_DASHBOARD_URL;
                case BankingConstants.ROLE_CUSTOMER -> BankingConstants.CUSTOMER_DASHBOARD_URL;
                default -> BankingConstants.LOGIN_URL;
            };
        }
        return BankingConstants.LOGIN_URL;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
