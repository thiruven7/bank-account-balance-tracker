/**
 * 
 */
package com.bankaccount.transactionproducer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.bankaccount.transactionproducer.dto.Transaction;

/**
 * Test class for TransactionGeneratorService
 */
@ExtendWith(MockitoExtension.class)
class TransactionGeneratorServiceTest {

	private TransactionGeneratorService transactionGeneratorService;

	@BeforeEach
	void setup() {
		transactionGeneratorService = new TransactionGeneratorService();
		ReflectionTestUtils.setField(transactionGeneratorService, "minAmount", BigDecimal.valueOf(200));
		ReflectionTestUtils.setField(transactionGeneratorService, "maxAmount", BigDecimal.valueOf(500000));
	}

	/**
	 * Verifies Generate Credit Transaction
	 */
	@Test
	void testGenerateCreditTransaction() {
		// when
		Transaction transaction = transactionGeneratorService.generateCredit();

		// then
		assertNotNull(transaction);
		assertTrue(transaction.getTransactionId().startsWith("CRE-"));
		assertTrue(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0);
		assertTrue(transaction.getAmount().compareTo(BigDecimal.valueOf(200)) >= 0);
		assertTrue(transaction.getAmount().compareTo(BigDecimal.valueOf(500000)) <= 0);
	}

	/**
	 * Verifies Generate Credit Transaction
	 */
	@Test
	void testGenerateDebitTransaction() {
		// when
		Transaction transaction = transactionGeneratorService.generateDebit();

		// then
		assertNotNull(transaction);
		assertTrue(transaction.getTransactionId().startsWith("DEB-"));
		assertTrue(transaction.getAmount().compareTo(BigDecimal.ZERO) < 0);
		assertTrue(transaction.getAmount().abs().compareTo(BigDecimal.valueOf(200)) >= 0);
		assertTrue(transaction.getAmount().abs().compareTo(BigDecimal.valueOf(500000)) <= 0);
	}

	/**
	 * Verifies that the fixed amount range is handled
	 */
	@Test
	void testFixedAmountRange() {

		// given
		ReflectionTestUtils.setField(transactionGeneratorService, "minAmount", BigDecimal.valueOf(150));
		ReflectionTestUtils.setField(transactionGeneratorService, "maxAmount", BigDecimal.valueOf(150));

		// when
		Transaction transCredit = transactionGeneratorService.generateCredit();
		Transaction transaDebit = transactionGeneratorService.generateDebit();

		// then
		assertNotNull(transCredit);
		assertEquals(BigDecimal.valueOf(150.00).setScale(2), transCredit.getAmount());
		assertNotNull(transaDebit);
		assertEquals(BigDecimal.valueOf(-150.00).setScale(2), transaDebit.getAmount());
	}

}
