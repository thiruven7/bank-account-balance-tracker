/**
 * 
 */
package com.bankaccount.balancetracker.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for balance tracker API errors.
 */
public class BalanceTrackerException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final HttpStatus status;

	public BalanceTrackerException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
