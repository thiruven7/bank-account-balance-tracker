package com.bankaccount.balancetracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.AuditSubmissionService;

/**
 * Test class to test BankAccountServiceImpl class
 */
@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

	@InjectMocks
	private BankAccountServiceImpl bankAccountServiceImpl;

	@Mock
	private AuditSubmissionService auditSubmissionService;

	@BeforeEach
	void setup() {
		ReflectionTestUtils.setField(bankAccountServiceImpl, "transactionLimit", 5);
	}

	/**
	 * Verifies balance and the audit submission is not triggered when the
	 * transaction count is under the configured limit
	 */
	@Test
	void testProcessTransactionAtBelowLimit() {

		// given
		Transaction trans = Transaction.builder().transactionId("CRE123").amount(new BigDecimal("250.52")).build();

		// when
		bankAccountServiceImpl.processTransaction(trans);

		// then
		double balance = bankAccountServiceImpl.retrieveBalance();
		assertEquals(250.52, balance, 0.001);
		verify(auditSubmissionService, never()).submit(anyList());

	}

	/**
	 * Verifies balance and the audit submission is not triggered when the
	 * transaction count is under the configured limit for the debit transaction
	 */
	@Test
	void testProcessTransactionWithDebitTransaction() {

		// given
		Transaction trans = Transaction.builder().transactionId("DEB123").amount(new BigDecimal("-250.52")).build();

		// when
		bankAccountServiceImpl.processTransaction(trans);

		// then
		double balance = bankAccountServiceImpl.retrieveBalance();
		assertEquals(-250.52, balance, 0.001);
		verify(auditSubmissionService, never()).submit(anyList());

	}

	/**
	 * Verifies balance and the audit submission is triggered when the transaction
	 * count is equal to the configured limit
	 */
	@Test
	void testProcessTransactionAtMaxLimit() {

		// given
		List<Transaction> transactions = List.of(new Transaction("CRE123", new BigDecimal("250")),
				new Transaction("CRE124", new BigDecimal("250")), new Transaction("DEB125", new BigDecimal("-300")),
				new Transaction("CRE126", new BigDecimal("200")), new Transaction("DEB127", new BigDecimal("-100.63")));

		// when
		transactions.forEach(bankAccountServiceImpl::processTransaction);

		// then
		double balance = bankAccountServiceImpl.retrieveBalance();
		assertEquals(299.37, balance, 0.001);
		verify(auditSubmissionService, times(1)).submit(transactions);

	}

	/**
	 * Verifies the retrieve balance is Zero when no transaction made.
	 */
	@Test
	void testRetrieveBalanceWithZeroBalance() {
		// when
		double balance = bankAccountServiceImpl.retrieveBalance();
		// then
		assertEquals(0.0, balance, 0.001);
	}

}
