package com.bankaccount.balancetracker.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.bankaccount.balancetracker.entity.TransactionT;
import com.bankaccount.balancetracker.repository.TransactionRepository;
import com.bankaccount.balancetracker.service.AuditSubmissionService;

/**
 * Test class to test the Audit Submission Scheduler function.
 */
@ExtendWith(MockitoExtension.class)
class AuditSubmissionSchedulerTest {

	@InjectMocks
	AuditSubmissionScheduler auditSubmissionScheduler;

	@Mock
	AuditSubmissionService auditSubmissionService;

	@Mock
	TransactionRepository transactionRepository;

	@BeforeEach
	void setup() {
		ReflectionTestUtils.setField(auditSubmissionScheduler, "transactionLimit", 2);
	}

	/**
	 * Verifies Scheduler when the transaction limit met.
	 */
	@DisplayName("Should submit pending transactions when limit is met and mark them as processed")
	@Test
	void testSubmitPendingTransactionsWhenLimitIsMet() {

		// given
		TransactionT transT1 = TransactionT.builder().accountId("CRE1235").accountId("ACC123456")
				.amount(new BigDecimal("250")).auditStatus("PENDING").updatedDateTime(LocalDateTime.now()).build();
		TransactionT transT2 = TransactionT.builder().accountId("DEB1255").accountId("ACC123456")
				.amount(new BigDecimal("-100")).auditStatus("PENDING").updatedDateTime(LocalDateTime.now()).build();
		List<TransactionT> pendingTrans = List.of(transT1, transT2);
		Page<TransactionT> page = new PageImpl<TransactionT>(pendingTrans);

		when(transactionRepository.findPendingTransactions(any())).thenReturn(page);

		// when
		auditSubmissionScheduler.submitPendingTransactions();

		// then

		verify(auditSubmissionService, times(1)).submit(anyList());
		verify(transactionRepository, times(1)).saveAll(anyList());

	}

	/**
	 * Verifies Scheduler when the transaction limit NOT met.
	 */
	@DisplayName("Should skip audit submission when pending transaction count is below limit")
	@Test
	void testSubmitPendingTransactionsWhenLimitNotMet() {

		// given
		TransactionT transT1 = TransactionT.builder().accountId("CRE1235").accountId("ACC123456")
				.amount(new BigDecimal("250")).auditStatus("PENDING").updatedDateTime(LocalDateTime.now()).build();
		List<TransactionT> pendingTrans = List.of(transT1);
		Page<TransactionT> page = new PageImpl<TransactionT>(pendingTrans);

		when(transactionRepository.findPendingTransactions(any())).thenReturn(page);

		// when
		auditSubmissionScheduler.submitPendingTransactions();

		// then

		verify(auditSubmissionService, times(0)).submit(anyList());
		verify(transactionRepository, times(0)).saveAll(anyList());

	}

	/**
	 * Verifies Scheduler when the pending transactions not available.
	 */
	@DisplayName("Should skip audit submission when pending transaction count is null")
	@Test
	void testSubmitPendingTransactionsWhenPendingTransactionNotAvailable() {

		// given
		List<TransactionT> pendingTrans = List.of();
		Page<TransactionT> page = new PageImpl<TransactionT>(pendingTrans);

		when(transactionRepository.findPendingTransactions(any())).thenReturn(page);

		// when
		auditSubmissionScheduler.submitPendingTransactions();

		// then

		verify(auditSubmissionService, times(0)).submit(anyList());
		verify(transactionRepository, times(0)).saveAll(anyList());

	}
}
