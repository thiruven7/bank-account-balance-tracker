/**
 * 
 */
package com.bankaccount.balancetracker.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.bankaccount.balancetracker.dto.ErrorResponse;

/**
 * Test class to validate exception handler methods in the
 * GlobalExceptionHandler class.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

	@Mock
	BindingResult bindingResult;

	private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

	/**
	 * Verifies validation exception handler
	 */
	@Test
	void testHandleValidationException() {
		// given
		FieldError error1 = new FieldError("transaction", "transactionId", "Transaction ID must not be null or empty");
		FieldError error2 = new FieldError("transaction", "amount", "Amount must not be null");
		List<FieldError> fieldErrors = List.of(error1, error2);
		when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
		MethodArgumentNotValidException argumentNotValidException = new MethodArgumentNotValidException(null,
				bindingResult);
		// when
		ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(argumentNotValidException);

		// then
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(400, response.getBody().getStatus());
		assertEquals("Validation failed", response.getBody().getMessage());
		assertNotNull(response.getBody().getTimestamp());
		assertEquals(2, response.getBody().getErrors().size());
		assertEquals("Transaction ID must not be null or empty", response.getBody().getErrors().get("transactionId"));
		assertEquals("Amount must not be null", response.getBody().getErrors().get("amount"));
	}

	/**
	 * Verifies balance tracker exception handler
	 */
	@Test
	void testHandleBalanceTrackerCustomException() {
		// given
		BalanceTrackerException balanceTrackerException = new BalanceTrackerException(
				"Balance not found for account Id ACC123456", HttpStatus.NOT_FOUND);
		// when
		ResponseEntity<ErrorResponse> response = exceptionHandler
				.handleBalanceTrackerException(balanceTrackerException);

		// then
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(404, response.getBody().getStatus());
		assertEquals("Balance not found for account Id ACC123456", response.getBody().getMessage());
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
