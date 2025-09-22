package com.bankaccount.balancetracker.integerationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.bankaccount.balancetracker.dto.BalanceResponse;
import com.bankaccount.balancetracker.dto.ErrorResponse;
import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.entity.BalanceT;
import com.bankaccount.balancetracker.entity.TransactionT;
import com.bankaccount.balancetracker.repository.BalanceRepository;
import com.bankaccount.balancetracker.repository.TransactionRepository;

/**
 * Integration test for Balance Tracker API with H2 DB integration
 */
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
class BalanceTrackerApi_IT {

	@Autowired
	TestRestTemplate testRestTemplate;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	BalanceRepository balanceRepository;

	@BeforeEach
	void resetBalance() {
		balanceRepository.deleteAll();
	}

	/**
	 * Verifies process transaction operation for a valid Transaction
	 */
	@DisplayName("Should return 404 when retrieving balance with no transactions")
	@Test
	void testRetrieveBalanceAtZeroBalance() {

		// when
		ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity("/api/bankaccount/v1/balance",
				ErrorResponse.class);

		// then
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals(404, response.getBody().getStatus());
		assertTrue(response.getBody().getMessage().contains("Balance not found for account Id"));
		assertNotNull(response.getBody().getTimestamp());
	}

	/**
	 * Verifies process transaction operation for a valid Transaction
	 */
	@DisplayName("Should process valid transaction and persist with correct audit status and balance")
	@Test
	void testProcessTransactionWithValidTransaction() {

		// given
		BigDecimal transAmount = new BigDecimal("250.52");
		Transaction trans = Transaction.builder().transactionId("CRE12312").amount(transAmount).build();

		// when
		ResponseEntity<Void> response = testRestTemplate.postForEntity("/api/bankaccount/v1/transactions", trans,
				Void.class);

		// then
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Optional<TransactionT> transactionT = transactionRepository.findById("CRE12312");
		assertTrue(transactionT.isPresent());
		TransactionT transEntity = transactionT.get();
		assertEquals("CRE12312", transEntity.getTransactionId());
		assertNotNull(transEntity.getAccountId());
		assertEquals(transAmount, transEntity.getAmount());
		assertNotNull(transEntity.getUpdatedDateTime());
		assertEquals("PENDING", transEntity.getAuditStatus());

		Optional<BalanceT> balanceT = balanceRepository.findById(transEntity.getAccountId());
		assertTrue(balanceT.isPresent());
		BalanceT balanceEntity = balanceT.get();
		assertEquals(0, transAmount.compareTo(balanceEntity.getAmount()));

	}

	/**
	 * Verifies process transaction operation for a invalid Id Transaction
	 */
	@DisplayName("Should reject transaction with blank transaction Id and return validation error")
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
	@DisplayName("Should reject transaction with invalid amount and return validation error")
	@Test
	void testProcessTransactionWithInvalidAmount() {

		// given
		Transaction trans = Transaction.builder().transactionId("CRE12313").amount(null).build();

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
	 * Verifies process transaction operation for a valid Transaction
	 */
	@DisplayName("Should retrieve correct balance after credit and debit transactions")
	@Test
	void testRetrieveBalanceAfterCreditAndDebitTransactions() {

		// given
		Transaction creditTx = Transaction.builder().transactionId("CRE12314").amount(new BigDecimal("250.52")).build();
		Transaction debitTx = Transaction.builder().transactionId("DEB23414").amount(new BigDecimal("-50.52")).build();

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

		Optional<TransactionT> transactionT = transactionRepository.findById("CRE12314");
		assertTrue(transactionT.isPresent());
		TransactionT transEntity = transactionT.get();
		assertEquals("CRE12314", transEntity.getTransactionId());
		assertNotNull(transEntity.getAccountId());
		assertEquals(new BigDecimal("250.52"), transEntity.getAmount());
		assertNotNull(transEntity.getUpdatedDateTime());
		assertEquals("PENDING", transEntity.getAuditStatus());

		Optional<TransactionT> transT = transactionRepository.findById("DEB23414");
		assertTrue(transT.isPresent());
		TransactionT transactionEntity = transT.get();
		assertEquals("DEB23414", transactionEntity.getTransactionId());
		assertNotNull(transactionEntity.getAccountId());
		assertEquals(new BigDecimal("-50.52"), transactionEntity.getAmount());
		assertNotNull(transEntity.getUpdatedDateTime());
		assertEquals("PENDING", transactionEntity.getAuditStatus());

		Optional<BalanceT> balanceT = balanceRepository.findById(transEntity.getAccountId());
		assertTrue(balanceT.isPresent());
		BalanceT balanceEntity = balanceT.get();
		assertEquals(new BigDecimal("200.00"), balanceEntity.getAmount());

	}

}
