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
	 * Validation exception handler
	 * 
	 * @param ex MethodArgumentNotValidException
	 * @return 400 Bad Request
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage()));
		log.warn("Validation error: {}", errors);
		ErrorResponse errorResp = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).message("Validation failed").errors(errors).build();
		return ResponseEntity.badRequest().body(errorResp);
	}
}
