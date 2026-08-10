package com.banking.exception;

import com.banking.util.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler providing consistent error responses for both
 * REST API endpoints (JSON) and Thymeleaf web endpoints (HTML).
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * BUG FIX: Re-throw AccessDeniedException so Spring Security's
     * ExceptionTranslationFilter can handle it and call the configured
     * CustomAccessDeniedHandler (→ HTTP 403).
     *
     * Previously the catch-all handleGenericException() below would absorb
     * AccessDeniedException and return HTTP 500, completely bypassing the
     * Spring Security access-denied flow and confusing both users and logs.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException ex) throws AccessDeniedException {
        // Re-throwing lets Spring Security's filter handle it correctly (403 / redirect).
        throw ex;
    }

    /**
     * Handles Jakarta Validation errors from @Valid / @Validated annotated parameters.
     *
     * BUG FIX: The original code computed the field-error map but then discarded it,
     * returning only the generic "Validation failed" message. Clients had no way to
     * know which fields were rejected. The errors map is now included in the response
     * data so the caller can highlight the exact failing fields.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message   = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        log.warn("Validation errors: {}", errors);

        // BUG FIX: use the constructor directly so we can include the errors map.
        // ApiResponse.error() only carries a message and no data payload.
        ApiResponse<Map<String, String>> body = new ApiResponse<>(
                false,
                "Validation failed",
                errors,                         // ← field-level details now returned
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles constraint violations from @Validated on method parameters.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotFound(AccountNotFoundException ex) {
        log.warn("Account not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalance(InsufficientBalanceException ex) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUser(DuplicateUserException ex) {
        log.warn("Duplicate user: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        log.warn("Business validation: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(AccountFrozenException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountFrozen(AccountFrozenException ex) {
        log.warn("Account frozen: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleDailyLimit(DailyLimitExceededException ex) {
        log.warn("Daily limit exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    /**
     * Catch-all for unexpected runtime errors.
     *
     * NOTE: AccessDeniedException is now handled by handleAccessDenied() above,
     * so it will never reach this method. All other exceptions that propagate
     * past the specific handlers land here and get a 500 response.
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error for request [{}]: {}", request.getDescription(false), ex.getMessage(), ex);

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "An unexpected error occurred. Please try again later.",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "An unexpected error occurred. Please try again later.");
        return mav;
    }
}
