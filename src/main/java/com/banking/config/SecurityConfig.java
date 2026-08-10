package com.banking.config;

import com.banking.security.*;
import com.banking.util.BankingConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginSuccessHandler      loginSuccessHandler;
    private final LoginFailureHandler      loginFailureHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;

    /**
     * BUG FIX: the remember-me signing key is now read from application.yml
     * (or the BANKING_REMEMBER_ME_KEY environment variable). The previous
     * hard-coded value "banking-remember-me-secret-key-2024" was visible in
     * a public repository, allowing anyone to forge persistent "remember me"
     * cookies for any user account.
     *
     * <p>The default value here is safe only for local development — always
     * set a strong random secret (≥ 32 chars) via environment variable in
     * any deployed environment.
     */
    @Value("${banking.security.remember-me-key:banking-remember-me-dev-key-change-in-prod}")
    private String rememberMeKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BankingConstants.BCRYPT_STRENGTH);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login", "/register", "/error", "/access-denied",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/actuator/health", "/actuator/prometheus",
                    "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/admin/**").hasAuthority(BankingConstants.ROLE_ADMIN)
                .requestMatchers("/api/v1/admin/**").hasAuthority(BankingConstants.ROLE_ADMIN)
                .requestMatchers("/employee/**").hasAuthority(BankingConstants.ROLE_EMPLOYEE)
                .requestMatchers("/api/v1/employee/**").hasAuthority(BankingConstants.ROLE_EMPLOYEE)
                .requestMatchers("/customer/**").hasAuthority(BankingConstants.ROLE_CUSTOMER)
                .requestMatchers("/api/v1/customer/**").hasAuthority(BankingConstants.ROLE_CUSTOMER)
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )

            .rememberMe(remember -> remember
                // BUG FIX: key is injected from config, not hard-coded
                .key(rememberMeKey)
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                .userDetailsService(userDetailsService)
                .rememberMeParameter("rememberMe")
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?expired")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )

            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect(request.getContextPath() + "/login"))
            )

            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/v1/**", "/actuator/**")
            )

            .headers(headers -> headers
                .xssProtection(xss -> xss.disable())
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                    "img-src 'self' data: https:; " +
                    "connect-src 'self';"
                ))
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(frame -> frame.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
            );

        http.authenticationProvider(authenticationProvider());
        return http.build();
    }
}
