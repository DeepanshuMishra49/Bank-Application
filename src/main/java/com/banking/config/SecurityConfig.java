package com.banking.config;

import com.banking.security.*;
import com.banking.util.BankingConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Enterprise-grade Spring Security configuration.
 *
 * <p>Provides:
 * <ul>
 *   <li>BCrypt password encoding</li>
 *   <li>CSRF protection (enabled)</li>
 *   <li>Security headers (XSS, CSP, referrer policy)</li>
 *   <li>Session fixation protection</li>
 *   <li>Role-based URL authorization</li>
 *   <li>Custom login/logout handlers</li>
 *   <li>Remember-me support</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;

    /**
     * BCrypt password encoder with strength 12 for production-grade hashing.
     *
     * @return configured PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BankingConstants.BCRYPT_STRENGTH);
    }

    /**
     * DAO authentication provider wiring the UserDetailsService and PasswordEncoder.
     *
     * @return configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Authentication manager exposing the global AuthenticationManager bean.
     *
     * @param authConfig Spring's AuthenticationConfiguration
     * @return the AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Main security filter chain defining all URL security rules, CSRF,
     * session management, security headers, and authentication endpoints.
     *
     * @param http the HttpSecurity to configure
     * @return the built SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ─── Authorization ────────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers(
                    "/login", "/register", "/error", "/access-denied",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/actuator/health", "/actuator/prometheus",
                    "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                ).permitAll()
                // Admin-only
                .requestMatchers("/admin/**").hasAuthority(BankingConstants.ROLE_ADMIN)
                .requestMatchers("/api/v1/admin/**").hasAuthority(BankingConstants.ROLE_ADMIN)
                // Employee-only
                .requestMatchers("/employee/**").hasAuthority(BankingConstants.ROLE_EMPLOYEE)
                .requestMatchers("/api/v1/employee/**").hasAuthority(BankingConstants.ROLE_EMPLOYEE)
                // Customer-only
                .requestMatchers("/customer/**").hasAuthority(BankingConstants.ROLE_CUSTOMER)
                .requestMatchers("/api/v1/customer/**").hasAuthority(BankingConstants.ROLE_CUSTOMER)
                // Any authenticated user
                .anyRequest().authenticated()
            )

            // ─── Form Login ───────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
                .permitAll()
            )

            // ─── Logout ───────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )

            // ─── Remember Me ──────────────────────────────────────────────────
            .rememberMe(remember -> remember
                .key("banking-remember-me-secret-key-2024")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                .userDetailsService(userDetailsService)
                .rememberMeParameter("rememberMe")
            )

            // ─── Session Management ───────────────────────────────────────────
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?expired")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )

            // ─── Exception Handling ───────────────────────────────────────────
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect(request.getContextPath() + "/login");
                })
            )

            // ─── CSRF ─────────────────────────────────────────────────────────
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/v1/**", "/actuator/**")
            )

            // ─── Security Headers ─────────────────────────────────────────────
            .headers(headers -> headers
                .xssProtection(xss -> xss.disable()) // CSP handles XSS
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                        "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self';"
                    )
                )
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .frameOptions(frame -> frame.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            );

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
