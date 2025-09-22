/**
 * 
 */
package com.bankaccount.balancetracker.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.entity.TransactionT;
import com.bankaccount.balancetracker.repository.TransactionRepository;
import com.bankaccount.balancetracker.service.AuditSubmissionService;

import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler to fetch the transactions based on the scheduler config limit and
 * splitting to batches and submit to the Audit System. It runs at a fixed delay
 * configured via application properties
 */
@Component
@Slf4j
public class AuditSubmissionScheduler {

	private static final String PROCESSED = "PROCESSED";
	private final TransactionRepository transactionRepository;
	private final AuditSubmissionService auditSubmissionService;

	@Value("${msa.auditsystem.transaction.limit}")
	private int transactionLimit;

	public AuditSubmissionScheduler(TransactionRepository transactionRepository,
			AuditSubmissionService auditSubmissionService) {
		this.transactionRepository = transactionRepository;
		this.auditSubmissionService = auditSubmissionService;

	}

	/**
	 * Scheduled method that triggers audit submission.It only proceeds if exactly
	 * `transactionLimit` pending transactions are available. Transactions are
	 * submitted in batches and marked as processed.
	 */
	@Scheduled(fixedDelayString = "${msa.auditsystem.scheduler.delay-ms}")
	@Transactional
	public void submitPendingTransactions() {
		log.info("submitPendingTransactions: entry");
		log.info("AuditSubmissionScheduler: Triggered at {}", LocalDateTime.now());

		// Fetch oldest pending transactions up to configured limit
		// order
		Page<TransactionT> page = transactionRepository.findPendingTransactions(PageRequest.of(0, transactionLimit));
		List<TransactionT> pendingTransactions = page.getContent();

		// Skip if count doesn't match configured limit
		if (pendingTransactions.size() != transactionLimit) {
			if (pendingTransactions.isEmpty()) {
				log.info("Skipping Audit Submission: No pending transactions found for audit submission");
			} else {
				log.info("Skipping Audit Submission: expected limit {}, found {}", transactionLimit,
						pendingTransactions.size());
			}
			return;
		}

		// Map entities to DTO for submission
		List<Transaction> dtoList = pendingTransactions.stream()
				.map(t -> new Transaction(t.getTransactionId(), t.getAmount())).toList();

		// Submit to audit system
		auditSubmissionService.submit(dtoList);

		// Mark transactions as processed
		pendingTransactions.forEach(t -> t.setAuditStatus(PROCESSED));
		transactionRepository.saveAll(pendingTransactions);
		log.info("AuditSubmissionScheduler: Submitted {} transactions and marked as Processed at {}", dtoList.size(),
				LocalDateTime.now());

	}

}
