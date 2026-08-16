package com.demo.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.demo.audit.AuditService;
import com.demo.common.ApiResponse;
import com.demo.transaction.dto.TransactionResponseDto;
import com.demo.transaction.dto.TransactionStatus;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AuditService auditService;

    public GlobalExceptionHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<TransactionResponseDto> handleInsufficientFunds(InsufficientFundsException ex) {
        TransactionResponseDto errorData = TransactionResponseDto.builder()
                .status((TransactionStatus.FAILED))
                .message(ex.getMessage())
                .build();

        auditService.logAction("user-test", "WITHDRAWAL_FAILED", "Failed withdrawal attempt" + ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorData);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String errorMessage = "A database constraint violation occurred.";
        String errorCode = "DATABASE_ERROR";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";

        if (rootMessage.contains("Duplicate entry") && rootMessage.contains("uc_user_email")) {
            errorMessage = "A user with this email already exists.";
            errorCode = "DUPLICATE_EMAIL";
            status = HttpStatus.CONFLICT; // 409 Conflict is ideal for duplicate entries
        }

        else if (rootMessage.contains("a foreign key constraint fails")) {
            errorMessage = "The referenced parent record could not be found.";
            errorCode = "FOREIGN_KEY_VIOLATION";
        }

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        String cleanErrorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(cleanErrorMessage, "VALIDATION_ERROR"));
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
