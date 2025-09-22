/**
 * 
 */
package com.bankaccount.balancetracker.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class to validate add transaction method.
 */
@ExtendWith(MockitoExtension.class)
class BatchDtoTest {

	/**
	 * Verifies method when the TotalTransactionValues is present
	 */
	@Test
	void testAddTransactionWhenTotalIsPresent() {
		// given
		Batch batch = Batch.builder().totalValueOfAllTransactions(new BigDecimal("200.00")).countOfTransactions(1)
				.build();
		// when
		batch.addTransaction(new BigDecimal("50.00"));
		// then
		assertEquals(new BigDecimal("250.00"), batch.getTotalValueOfAllTransactions());
		assertEquals(2, batch.getCountOfTransactions());

	}

	/**
	 * Verifies method when the TotalTransactionValues is NOT present, set to Zero.
	 */
	@Test
	void testAddTransactionWhenTotalNull() {

		// given
		Batch batch = new Batch();

		// when
		batch.addTransaction(new BigDecimal("222.00"));

		// then
		assertEquals(new BigDecimal("222.00"), batch.getTotalValueOfAllTransactions());
		assertEquals(1, batch.getCountOfTransactions());

	}
}
