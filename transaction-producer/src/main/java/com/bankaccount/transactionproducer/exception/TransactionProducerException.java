/**
 * 
 */
package com.bankaccount.transactionproducer.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for transaction producer service errors.
 */
public class TransactionProducerException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final HttpStatus status;

	public TransactionProducerException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
