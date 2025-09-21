package com.bankaccount.balancetracker.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.AuditSubmissionService;
import com.bankaccount.balancetracker.service.BankAccountService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service Implementation class for the bank account operations
 */
@Service
@Qualifier("inMemoryService")
@Slf4j
public class InMemoryBankAccountServiceImpl implements BankAccountService {

	private final Queue<Transaction> transactionsQueue = new ConcurrentLinkedQueue<>();
	private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);
	private final Object lock = new Object();

	@Value("${msa.auditsystem.transaction.limit}")
	private int transactionLimit;

	private final AuditSubmissionService auditSubmissionService;

	public InMemoryBankAccountServiceImpl(AuditSubmissionService auditSubmissionService) {
		this.auditSubmissionService = auditSubmissionService;
	}

	/**
	 * Process Transaction
	 */
	@Override
	public void processTransaction(Transaction transaction) {
		log.debug("processTransaction:enter with transaction Id: {}", transaction.getTransactionId());

		// Update Balance
		var updatedBalance = balance.updateAndGet(bal -> bal.add(transaction.getAmount()));
		log.debug("Updated balance after transaction Id {}: {}", transaction.getTransactionId(), updatedBalance);

		// Add transactions to the queue
		transactionsQueue.add(transaction);

		// Check the Transaction limit and submit to Audit System
		if (transactionsQueue.size() >= transactionLimit) {
			// To ensure thread safe
			synchronized (lock) {
				log.info("Inside Sync Lock for transactioId: {} at {}", transaction.getTransactionId(),
						LocalDateTime.now());
				if (transactionsQueue.size() >= transactionLimit) {
					List<Transaction> submissionTransList = new ArrayList<>();
					// Poll Transactions from the Queue
					for (int i = 0; i < transactionLimit; i++) {
						Optional.ofNullable(transactionsQueue.poll()).ifPresent(submissionTransList::add);
					}
					// Submit to Audit System with the transaction limit reached
					if (!submissionTransList.isEmpty()) {
						log.info("Transaction limit reached ({}). Triggering audit submission.", transactionLimit);
						auditSubmissionService.submit(submissionTransList);
					}
				}
			}

		}
		log.debug("processTransaction:exit");

	}

	/**
	 * Retrieve Balance
	 */
	@Override
	public double retrieveBalance() {
		log.debug("retrieveBalance:entry");
		return balance.get().doubleValue();
	}

}
