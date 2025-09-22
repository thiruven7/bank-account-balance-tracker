/**
 * 
 */
package com.bankaccount.transactionproducer.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bankaccount.transactionproducer.dto.ErrorResponse;

/**
 * Test class to validate exception handler methods in the
 * GlobalExceptionHandler class.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

	/**
	 * Verifies balance tracker exception handler
	 */
	@Test
	void testHandleTransactionProducerCustomException() {
		// given
		TransactionProducerException producerException = new TransactionProducerException("Producer is already running",
				HttpStatus.CONFLICT);
		// when
		ResponseEntity<ErrorResponse> response = exceptionHandler.handleTransactionProducerException(producerException);

		// then
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(409, response.getBody().getStatus());
		assertEquals("Producer is already running", response.getBody().getMessage());
		assertNotNull(response.getBody().getTimestamp());
	}

	/**
	 * Verifies Generic exception handler
	 */
	@Test
	void testHandleGenericException() {
		// given
		Exception exception = new RuntimeException("Unexpected failure occured");

		// when
		ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

		// then
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(500, response.getBody().getStatus());
		assertEquals("Internal server error", response.getBody().getMessage());
		assertNotNull(response.getBody().getTimestamp());
	}

}
