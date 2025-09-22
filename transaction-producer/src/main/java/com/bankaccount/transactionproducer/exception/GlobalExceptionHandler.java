package com.bankaccount.transactionproducer.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bankaccount.transactionproducer.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler class for the balance-tracker-api service
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 
	 * /** Custom Exception handler
	 */
	@ExceptionHandler(TransactionProducerException.class)
	public ResponseEntity<ErrorResponse> handleTransactionProducerException(TransactionProducerException tpe) {
		log.warn("TransactionProducerException: {}", tpe.getMessage());
		ErrorResponse errorResp = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(tpe.getStatus().value())
				.message(tpe.getMessage()).build();
		return ResponseEntity.status(tpe.getStatus()).body(errorResp);
	}

	/**
	 * 
	 * /** Generic Exception handler
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		log.warn("Unhandled Exception: {}", ex.getMessage(), ex);
		ErrorResponse errorResp = ErrorResponse.builder().timestamp(LocalDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Internal server error").build();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResp);
	}
}
