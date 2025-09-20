package com.bankaccount.balancetracker.dto;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Represents an error response with details")
public class ErrorResponse {

	/**
	 * Timestamp of the exception
	 */
	@Schema(description = "Timestamp of the error", example = "2025-09-20T12:55:44.592")
	private LocalDateTime timestamp;

	/**
	 * Http Status code
	 */
	@Schema(description = "HTTP status code", example = "400")
	private int status;

	/**
	 * Exception message
	 */
	@Schema(description = "Error message", example = "Error message")
	private String message;

	/**
	 * Collection of errors
	 */
	@Schema(description = "Field level validation errors")
	private Map<String, String> errors;

}
