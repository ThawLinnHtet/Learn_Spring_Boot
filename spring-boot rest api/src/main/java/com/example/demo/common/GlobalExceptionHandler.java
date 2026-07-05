package com.example.demo.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors(exception));
	}

	@ExceptionHandler(BindException.class)
	ResponseEntity<ApiErrorResponse> handleBind(BindException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors(exception));
	}

	@ExceptionHandler({ IllegalArgumentException.class, ConstraintViolationException.class })
	ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request, Map.of());
	}

	private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message, HttpServletRequest request,
			Map<String, String> fieldErrors) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fieldErrors);
		return ResponseEntity.status(status).body(response);
	}

	private Map<String, String> fieldErrors(BindException exception) {
		Map<String, String> errors = new LinkedHashMap<>();
		exception.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		return errors;
	}
}
