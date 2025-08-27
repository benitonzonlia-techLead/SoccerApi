package com.bnz.soccer.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Build a standard error response body
    private Map<String, Object> buildBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

    // ⚠️ Validation errors on @RequestBody with @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation failed");

        List<Map<String, Object>> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "field", err.getField(),
                        "rejectedValue", err.getRejectedValue(),
                        "message", err.getDefaultMessage()
                ))
                .toList();

        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    // ⚠️ Validation errors on @RequestParam, @PathVariable, etc.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("violations", ex.getConstraintViolations()
                .stream()
                .map(v -> Map.of(
                        "property", v.getPropertyPath().toString(),
                        "invalidValue", v.getInvalidValue(),
                        "message", v.getMessage()
                ))
                .toList());
        return ResponseEntity.badRequest().body(body);
    }

    // Bad parameter type, unreadable value, or missing parameter
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<?> handleBadRequest(Exception ex) {
        return ResponseEntity
                .badRequest()
                .body(buildBody(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // Entity not found
    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<?> handleTeamNotFound(TeamNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    // Simple business logic errors (IllegalArgument, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // Catch-all → Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"));
    }
}
