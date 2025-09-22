/**
 * 
 */
package com.bankaccount.balancetracker.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.bankaccount.balancetracker.dto.Batch;
import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.helper.AuditSystemBatchBuilder;

/**
 * Test class to test AuditSubmissionService implementation
 */
@ExtendWith(MockitoExtension.class)
class AuditSubmissionServiceImplTest {

	@InjectMocks
	private AuditSubmissionServiceImpl auditSubmissionServiceImpl;

	@Mock
	private AuditSystemBatchBuilder auditSystemBatchBuilder;

	@BeforeEach
	void setup() {
		ReflectionTestUtils.setField(auditSubmissionServiceImpl, "maxAmountPerBatch", new BigDecimal("500"));
	}

	/**
	 * Verifies the submit to Audit System functionality with valid transaction list
	 */
	@Test
	void testAuditSubmissionWithValidTrasnactionList() {

		// given
		List<Transaction> transactions = List.of(new Transaction("CRE1234", new BigDecimal("250")),
				new Transaction("CRE1244", new BigDecimal("250")), new Transaction("DEB1254", new BigDecimal("-300")),
				new Transaction("CRE1264", new BigDecimal("200")), new Transaction("DEB1274", new BigDecimal("-100.63")));

		when(auditSystemBatchBuilder.buildBatches(anyList(), any()))
				.thenReturn(List.of(new Batch(new BigDecimal("500"), 2), new Batch(new BigDecimal("500"), 2),
						new Batch(new BigDecimal("100.63"), 1)));
		// when
		auditSubmissionServiceImpl.submit(transactions);

		// then
		verify(auditSystemBatchBuilder, times(1)).buildBatches(anyList(), eq(new BigDecimal("500")));

	}

	/**
	 * Verifies the submit to Audit System functionality with empty transaction list
	 */
	@Test
	void testAuditSubmissionWithEmptyTrasnactionList() {

		// given
		List<Transaction> transactions = List.of();

		// when
		auditSubmissionServiceImpl.submit(transactions);

		// then
		verify(auditSystemBatchBuilder, times(0)).buildBatches(anyList(), eq(new BigDecimal("500")));
	}

	/**
	 * Verifies the submit to Audit System functionality with null as an argument
	 */
	@Test
	void testAuditSubmissionWithNull() {

		// given
		List<Transaction> transactions = null;

		// when
		auditSubmissionServiceImpl.submit(transactions);

		// then
		verify(auditSystemBatchBuilder, times(0)).buildBatches(anyList(), eq(new BigDecimal("500")));
	}

}
