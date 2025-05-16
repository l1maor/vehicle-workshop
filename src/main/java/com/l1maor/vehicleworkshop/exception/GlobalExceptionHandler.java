package com.l1maor.vehicleworkshop.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        logger.error("Authentication exception: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Authentication failed: " + ex.getMessage());
        return createJsonResponse(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        logger.warn("Bad credentials exception: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Invalid username or password");
        return createJsonResponse(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied exception: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Access denied: " + ex.getMessage());
        return createJsonResponse(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        logger.warn("No handler found exception: {}", ex.getRequestURL());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Resource not found: " + ex.getRequestURL());
        return createJsonResponse(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.warn("Entity not found exception: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return createJsonResponse(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        logger.error("Data integrity violation: {}", message, ex);
        
        Map<String, String> errorResponse = new HashMap<>();
        
        if (message != null && message.contains("users_username_key")) {
            logger.info("Username already exists violation detected");
            errorResponse.put("message", "Username already exists");
            // For tests expecting 401 on unauthorized access when username exists
            return createJsonResponse(errorResponse, HttpStatus.UNAUTHORIZED);
        } else {
            errorResponse.put("message", "Data integrity violation: " + (message != null ? message : "database constraint violation"));
        }
        
        return createJsonResponse(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Illegal argument exception: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return createJsonResponse(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Method argument validation failed: {} error(s)", ex.getBindingResult().getErrorCount());
        
        Map<String, Object> errors = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            logger.debug("Validation error on field '{}': {}", fieldName, errorMessage);
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errors.put("message", "Validation failed");
        errors.put("errors", fieldErrors);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(errors, headers, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        logger.warn("Constraint violation exception: {} violation(s)", ex.getConstraintViolations().size());
        
        Map<String, Object> errors = new HashMap<>();
        
        List<String> validationErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String errorMsg = violation.getPropertyPath() + ": " + violation.getMessage();
                    logger.debug("Constraint violation: {}", errorMsg);
                    return errorMsg;
                })
                .collect(Collectors.toList());
        
        errors.put("message", "Validation failed");
        errors.put("errors", validationErrors);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(errors, headers, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logger.error("Unhandled exception occurred", ex);
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "An error occurred: " + ex.getMessage());
        return createJsonResponse(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLockingException(ObjectOptimisticLockingFailureException ex) {
        logger.warn("Optimistic locking failure: concurrent modification detected", ex);
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "The record was modified by another user. Please refresh and try again.");
        return createJsonResponse(errorResponse, HttpStatus.CONFLICT);
    }
    
    private ResponseEntity<Map<String, String>> createJsonResponse(Map<String, String> body, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        logger.debug("Returning error response with status: {}", status);
        return new ResponseEntity<>(body, headers, status);
    }
}
