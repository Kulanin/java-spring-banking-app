package com.demo.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.demo.audit.AuditService;
import com.demo.common.ApiResponse;
import com.demo.transaction.dto.TransactionResponse;
import com.demo.transaction.dto.TransactionStatus;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AuditService auditService;

    public GlobalExceptionHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<TransactionResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        TransactionResponse response = new TransactionResponse(
                TransactionStatus.FAILED,
                0,
                ex.getMessage());

        auditService.logAction("user-test", "WITHDRAWAL_FAILED", "Failed withdrawal attempt" + ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String errorMessage = "A database constraint violation occurred.";
        String errorCode = "DATABASE_ERROR";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Extract the root SQL exception message
        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";

        // Check if the crash was caused by your unique email constraint
        if (rootMessage.contains("Duplicate entry") && rootMessage.contains("uc_user_email")) {
            errorMessage = "A user with this email already exists.";
            errorCode = "DUPLICATE_EMAIL";
            status = HttpStatus.CONFLICT; // 409 Conflict is ideal for duplicate entries
        }
        // Check for foreign key failures (e.g., creating an account for a user ID that
        // doesn't exist)
        else if (rootMessage.contains("a foreign key constraint fails")) {
            errorMessage = "The referenced parent record could not be found.";
            errorCode = "FOREIGN_KEY_VIOLATION";
        }

        // Return your structured global API response layout
        return ResponseEntity.status(status)
                .body(ApiResponse.error(errorMessage, errorCode));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        String message = String.format("Missing required header : %s", ex.getHeaderName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message, null));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidTransactionAmountException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidAmount(InvalidTransactionAmountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_TRANSACTION_AMOUNT"));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedException(UnsupportedOperationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeErrors(RuntimeException ex) {
        if (ex.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND"));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occured", "SERVER_ERROR"));
    }

}
