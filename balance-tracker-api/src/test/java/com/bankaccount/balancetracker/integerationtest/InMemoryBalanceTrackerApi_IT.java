package com.bankaccount.balancetracker.integerationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.bankaccount.balancetracker.dto.BalanceResponse;
import com.bankaccount.balancetracker.dto.ErrorResponse;
import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.AuditSubmissionService;

/**
 * Integration test for Balance Tracker API
 */
@Disabled("Disabled to focus on DB backed service integration. In memory logic tested separately.")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
class InMemoryBalanceTrackerApi_IT {

	@Autowired
	TestRestTemplate testRestTemplate;

	@SuppressWarnings("removal") // TO-DO: Could be replaced with manual TestConfig to create Spy Bean
	@SpyBean
	private AuditSubmissionService auditSubmissionService;

	/**
	 * Verifies process transaction operation for a valid Transaction
	 */
	@Test
	void testProcessTransactionWithValidTransaction() {

		// given
		Transaction trans = Transaction.builder().transactionId("CRE1236").amount(new BigDecimal("250.52")).build();

		// when
		ResponseEntity<Void> response = testRestTemplate.postForEntity("/api/bankaccount/v1/transactions", trans,
				Void.class);

		// then
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	/**
	 * Verifies process transaction operation for a invalid Id Transaction
	 */
	@Test
	void testProcessTransactionWithInvalidId() {

		// given
		Transaction trans = Transaction.builder().transactionId(" ").amount(new BigDecimal("250.52")).build();

		// when
		ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity("/api/bankaccount/v1/transactions",
				trans, ErrorResponse.class);

		// then
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(400, response.getBody().getStatus());
		assertEquals("Validation failed", response.getBody().getMessage());
		assertNotNull(response.getBody().getTimestamp());
		assertNotNull(response.getBody().getErrors());
		assertEquals(1, response.getBody().getErrors().size());
		assertTrue(response.getBody().getErrors().containsKey("transactionId"));
		assertEquals("Transaction ID must not be null or empty", response.getBody().getErrors().get("transactionId"));
	}

	/**
	 * Verifies process transaction operation for a invalid amount Transaction
	 */
	@Test
	void testProcessTransactionWithInvalidAmount() {

		// given
		Transaction trans = Transaction.builder().transactionId("CRE12378").amount(null).build();

		// when
		ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity("/api/bankaccount/v1/transactions",
				trans, ErrorResponse.class);

		// then
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(400, response.getBody().getStatus());
		assertEquals("Validation failed", response.getBody().getMessage());
		assertNotNull(response.getBody().getTimestamp());
		assertNotNull(response.getBody().getErrors());
		assertEquals(1, response.getBody().getErrors().size());
		assertTrue(response.getBody().getErrors().containsKey("amount"));
		assertEquals("Amount must not be null", response.getBody().getErrors().get("amount"));
	}

	/**
	 * Verifies process transaction operation and check audit submission for a valid
	 * transactions For test purpose the transactionLimit has been set to #5 and
	 * MaxAmountPerBatch has been set to £500
	 */
	@Test
	void testAuditSubmissionAtMaxLimitToSubmit() {

		// given
		List<Transaction> transactions = List.of(new Transaction("CRE1238", new BigDecimal("250")),
				new Transaction("CRE1248", new BigDecimal("250")), new Transaction("DEB1258", new BigDecimal("-300")),
				new Transaction("CRE1268", new BigDecimal("200")), new Transaction("DEB1278", new BigDecimal("-100.63")));

		// when
		transactions.forEach(trans -> {
			testRestTemplate.postForEntity("/api/bankaccount/v1/transactions", trans, Void.class);

		});

		// then
		ResponseEntity<BalanceResponse> response = testRestTemplate.getForEntity("/api/bankaccount/v1/balance",
				BalanceResponse.class);
		verify(auditSubmissionService, times(1)).submit(anyList());
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(299.37, response.getBody().getBalance());
	}

	/**
	 * Verifies process transaction operation for a valid Transaction
	 */
	@Test
	void testRetrieveBalanceAtZeroBalance() {

		// when
		ResponseEntity<BalanceResponse> response = testRestTemplate.getForEntity("/api/bankaccount/v1/balance",
				BalanceResponse.class);

		// then
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0.0, response.getBody().getBalance());
	}

	/**
	 * Verifies process transaction operation for a valid Transaction
	 */
	@Test
	void testRetrieveBalanceAfterCreditAndDebitTransactions() {

		// given
		Transaction creditTx = Transaction.builder().transactionId("CRE1239").amount(new BigDecimal("250.52")).build();
		Transaction debitTx = Transaction.builder().transactionId("DEB2349").amount(new BigDecimal("-50.52")).build();

		testRestTemplate.postForEntity("/api/bankaccount/v1/transactions", creditTx, Void.class);
		testRestTemplate.postForEntity("/api/bankaccount/v1/transactions", debitTx, Void.class);

		// when
		ResponseEntity<BalanceResponse> response = testRestTemplate.getForEntity("/api/bankaccount/v1/balance",
				BalanceResponse.class);

		// then
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(200.0, response.getBody().getBalance());
	}

}
