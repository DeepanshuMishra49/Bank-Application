package com.banking.util;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for all REST endpoints.
 * Ensures a consistent response shape across the entire API.
 *
 * @param <T> the type of the response data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp,
        int statusCode
) {

    /**
     * Creates a successful response with data.
     *
     * @param message the success message
     * @param data    the response payload
     * @param <T>     payload type
     * @return a successful ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), 200);
    }

    /**
     * Creates a successful response without data.
     *
     * @param message the success message
     * @return a successful ApiResponse with null data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now(), 200);
    }

    /**
     * Creates an error response.
     *
     * @param message    the error message
     * @param statusCode the HTTP status code
     * @param <T>        payload type
     * @return an error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), statusCode);
    }

    /**
     * Creates a created (201) response with data.
     *
     * @param message the success message
     * @param data    the created entity data
     * @param <T>     payload type
     * @return a created ApiResponse
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), 201);
    }
}
