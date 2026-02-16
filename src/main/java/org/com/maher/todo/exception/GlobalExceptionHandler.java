package org.com.maher.todo.exception;

import tools.jackson.core.JacksonException;
import lombok.extern.slf4j.Slf4j;
import org.com.maher.todo.api.model.ErrorResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TodoNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PastDueModificationException.class)
    public ResponseEntity<ErrorResponse> handlePastDue(PastDueModificationException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "The item was modified by another request. Please retry.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        String message = extractDetailMessage(ex);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    private String extractDetailMessage(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof JacksonException jacksonEx) {
            String fieldPath = jacksonEx.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .collect(Collectors.joining("."));

            Throwable rootCause = jacksonEx.getCause();
            String detail = rootCause != null ? rootCause.getMessage() : jacksonEx.getOriginalMessage();

            if (fieldPath != null && !fieldPath.isEmpty() && detail != null) {
                return "Invalid value for field '" + fieldPath + "': " + detail;
            }
            if (detail != null) {
                return detail;
            }
        }

        return "Malformed request body";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
        error.setMessage(message);
        return ResponseEntity.status(status).body(error);
    }
}
