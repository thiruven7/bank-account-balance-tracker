package com.bankaccount.balancetracker.service.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bankaccount.balancetracker.dto.Batch;
import com.bankaccount.balancetracker.dto.Transaction;

/**
 * Test class to test AuditSystemBatchBuilder class
 */
@ExtendWith(MockitoExtension.class)
class AuditSystemBatchBuilderTest {

	@InjectMocks
	private AuditSystemBatchBuilder auditSystemBatchBuilder;

	/**
	 * Verify the build batches method with mixed of debit and credit transactions
	 */
	@Test
	void testBuildBatchesWithMixedTransactions() {

		// given
		BigDecimal maxAmountPerBatch = new BigDecimal("500");
		List<Transaction> transactions = List.of(new Transaction("CRE123", new BigDecimal("250")),
				new Transaction("CRE124", new BigDecimal("250")), new Transaction("DEB125", new BigDecimal("-300")),
				new Transaction("CRE126", new BigDecimal("200")), new Transaction("DEB127", new BigDecimal("-100.63")));

		// when
		List<Batch> batches = auditSystemBatchBuilder.buildBatches(transactions, maxAmountPerBatch);

		// then
		assertEquals(3, batches.size());
		assertEquals(new BigDecimal("500"), batches.get(0).getTotalValueOfAllTransactions());
		assertEquals(2, batches.get(0).getCountOfTransactions());
		assertEquals(new BigDecimal("500"), batches.get(1).getTotalValueOfAllTransactions());
		assertEquals(2, batches.get(1).getCountOfTransactions());
		assertEquals(new BigDecimal("100.63"), batches.get(2).getTotalValueOfAllTransactions());
		assertEquals(1, batches.get(2).getCountOfTransactions());

	}

	/**
	 * Verify the build batches method with empty transactions list
	 */
	@Test
	void testBuildBatchesWithEmptyTransactionList() {

		// given
		BigDecimal maxAmountPerBatch = new BigDecimal("500");
		List<Transaction> transactions = List.of();

		// when
		List<Batch> batches = auditSystemBatchBuilder.buildBatches(transactions, maxAmountPerBatch);

		// then
		assertEquals(0, batches.size());
	}

	/**
	 * Verify the build batches method with transactions list as null
	 */
	@Test
	void testBuildBatchesWithTransactionListAsNull() {

		// given
		BigDecimal maxAmountPerBatch = new BigDecimal("500");
		List<Transaction> transactions = null;

		// when
		List<Batch> batches = auditSystemBatchBuilder.buildBatches(transactions, maxAmountPerBatch);

		// then
		assertEquals(0, batches.size());
	}

}
