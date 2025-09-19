package com.bankaccount.balancetracker.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class to represent error response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

	/**
	 * Timestamp of the exception
	 */
	private LocalDateTime timestamp;

	/**
	 * Http Status code
	 */
	private int status;

	/**
	 * Exception message
	 */
	private String message;

	/**
	 * Collection of errors
	 */
	private Map<String, String> errors;

}
