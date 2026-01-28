package com.bs_enterprises.enterprise_backend_template.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bs_enterprises.enterprise_backend_template.handlers.exceptions.KeycloakException;
import com.bs_enterprises.enterprise_backend_template.keys.MessageKeys;
import com.bs_enterprises.enterprise_backend_template.models.responses.ApiResponse;
import com.bs_enterprises.enterprise_backend_template.services.common.MessageUtils;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageUtils messageSource;

    public GlobalExceptionHandler(MessageUtils messageUtils) {
        this.messageSource = messageUtils;
    }

    // -----------------------
    // Existing simple handlers
    // -----------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalAccessException(IllegalAccessException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Void>> handleIOException(IOException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchPaddingException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchPaddingException(NoSuchPaddingException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalBlockSizeException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalBlockSizeException(IllegalBlockSizeException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchAlgorithmException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchAlgorithmException(NoSuchAlgorithmException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadPaddingException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadPaddingException(BadPaddingException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidKeyException(InvalidKeyException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return buildErrorResponse(ex, MessageKeys.ERROR_ACCESS_UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return buildErrorResponse(ex, MessageKeys.ERROR_ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(SecurityException ex) {
        return buildErrorResponse(ex, MessageKeys.ERROR_ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKeyException(DuplicateKeyException ex) {
        // keep as internal server error — you can map specific duplicate key messages to better codes if needed
        return buildErrorResponse(ex, MessageKeys.ERROR_UNKNOWN_CONTACT_ADMINISTRATOR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return buildErrorResponse(ex, MessageKeys.ERROR_KNOWN_HEADER_MISSING, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildErrorResponse(ex, MessageKeys.ERROR_UNKNOWN_CONTACT_ADMINISTRATOR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // -----------------------
    // Keycloak-specific handler
    // -----------------------
    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<ApiResponse<Void>> handleKeycloakAuthException(KeycloakException ex) {
        String rawMessage = ex.getDescription();
        String code = "unknown_error";
        String message = rawMessage;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.readValue(rawMessage, Map.class);

            // Case 1: Keycloak user creation error
            if (map.containsKey("errorMessage")) {
                code = "user_already_exists";
                message = (String) map.get("errorMessage");
            }
            // Case 2: Standard Keycloak OAuth errors
            else if (map.containsKey("error") || map.containsKey("error_description")) {
                code = (String) map.getOrDefault("error", code);
                message = (String) map.getOrDefault("error_description", message);
            }
        } catch (Exception ignored) {
            // Parsing failed → fallback to raw message
        }

        return new ResponseEntity<>(ApiResponse.failure(code, message, null), HttpStatus.UNAUTHORIZED);
    }

    // -----------------------
    // Validation handlers
    // -----------------------

    /**
     * Handles @Valid failures on @RequestBody (binding errors).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String field = error.getField();
            String raw = error.getDefaultMessage();
            String resolved = resolveMessageOrReturnRaw(raw);
            errors.put(field, resolved);
        }

        // include global errors if any
        ex.getBindingResult().getGlobalErrors()
                .forEach(globalError -> {
                    String raw = globalError.getDefaultMessage();
                    String resolved = resolveMessageOrReturnRaw(raw);
                    errors.put(globalError.getObjectName(), resolved);
                });

        String msg = messageSource.getMessageFromCode(MessageKeys.ERROR_VALIDATION);
        return new ResponseEntity<>(ApiResponse.failure(MessageKeys.ERROR_VALIDATION, msg, errors), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles parameter-level validation (e.g. @Validated on controller parameters).
     * Spring may wrap ConstraintViolationException in HandlerMethodValidationException.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException cve) {
            return handleConstraintViolationExceptionInternal(cve);
        } else {
            // fallback to a readable message
            String resolved = resolveMessageOrReturnRaw(ex.getMessage());
            return new ResponseEntity<>(ApiResponse.failure(MessageKeys.ERROR_PAYLOAD_VALIDATION, resolved, Map.of()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Direct ConstraintViolationException (may be thrown by validator for method params or elsewhere).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        return handleConstraintViolationExceptionInternal(ex);
    }

    private ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationExceptionInternal(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> lastPathNode(v.getPropertyPath().toString()),
                        v -> resolveMessageOrReturnRaw(v.getMessage()),
                        // on duplicate property keys, keep first
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        String msg = messageSource.getMessageFromCode(MessageKeys.ERROR_PAYLOAD_VALIDATION);
        return new ResponseEntity<>(ApiResponse.failure(MessageKeys.ERROR_PAYLOAD_VALIDATION, msg, errors), HttpStatus.BAD_REQUEST);
    }

    // -----------------------
    // Helpers
    // -----------------------

    /**
     * Build ApiResponse using code (key) or raw message — resolve using MessageUtils if possible.
     * 'code' may be a message key (like "{some.key}") or a raw string.
     */
    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(Exception ex, String codeOrMessage, HttpStatus status) {
        log.error("Exception handled: {}", ex.getClass().getSimpleName(), ex);

        String resolved = resolveMessageOrReturnRaw(codeOrMessage);
        String code = detectCodeToken(codeOrMessage).orElse(codeOrMessage);

        return new ResponseEntity<>(ApiResponse.failure(code, resolved, null), status);
    }

    /**
     * Try to resolve a message key via MessageUtils. If the input looks like "{key}" strip braces.
     * If resolution fails, return the raw input.
     */
    private String resolveMessageOrReturnRaw(String codeOrRaw) {
        if (codeOrRaw == null) return null;
        String candidate = codeOrRaw;
        // strip braces if present
        if (candidate.startsWith("{") && candidate.endsWith("}")) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }
        try {
            String resolved = messageSource.getMessageFromCode(candidate);
            if (resolved != null) return resolved;
        } catch (Exception ignored) {
            // fallthrough to return raw
        }
        return codeOrRaw;
    }

    /**
     * If input is a braced token like "{some.key}" return the inner key as code.
     */
    private Optional<String> detectCodeToken(String codeOrRaw) {
        if (codeOrRaw == null) return Optional.empty();
        if (codeOrRaw.startsWith("{") && codeOrRaw.endsWith("}")) {
            return Optional.of(codeOrRaw.substring(1, codeOrRaw.length() - 1));
        }
        // if it looks like a message key (contains dots with lower case) you might treat it as code.
        if (codeOrRaw.contains(".") && !codeOrRaw.contains(" ")) {
            return Optional.of(codeOrRaw);
        }
        return Optional.empty();
    }

    private String lastPathNode(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) return propertyPath;
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }
}
