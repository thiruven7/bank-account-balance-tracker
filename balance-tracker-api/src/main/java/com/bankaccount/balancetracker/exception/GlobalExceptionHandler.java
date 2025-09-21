package com.bankaccount.balancetracker.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bankaccount.balancetracker.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler class for the balance-tracker-api service
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * MethodArgumentNotValidException exception handler
	 * 
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage()));
		log.warn("Validation error: {}", errors);
		ErrorResponse errorResp = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).message("Validation failed").errors(errors).build();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
	}

	/**
	 * Custom BalanceTrackerException handler
	 * 
	 */
	@ExceptionHandler(BalanceTrackerException.class)
	public ResponseEntity<ErrorResponse> handleBalanceTrackerException(BalanceTrackerException ex) {
		log.warn("BalanceTrackerException: {}", ex.getMessage());
		ErrorResponse errorResp = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(ex.getStatus().value())
				.message(ex.getMessage()).build();
		return ResponseEntity.status(ex.getStatus()).body(errorResp);
	}

	/**
	 * Generic Exception handler
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		log.warn("Unhandled Exception: {}", ex.getMessage(), ex);
		ErrorResponse errorResp = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Internal server error").build();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResp);
	}
}
