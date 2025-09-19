package com.bankaccount.balancetracker.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.AuditSubmissionService;
import com.bankaccount.balancetracker.service.BankAccountService;
import com.bankaccount.balancetracker.service.helper.AuditSystemBatchBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Service Implementation class for the bank account operations
 */
@Service
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

	private final Queue<Transaction> transactionsQueue = new ConcurrentLinkedQueue<>();
	private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);
	private final Object lock = new Object();

	@Value("${msa.auditsystem.transaction.limit}")
	private int transactionLimit;

	private final AuditSubmissionService auditSubmissionService;

	public BankAccountServiceImpl(AuditSubmissionService auditSubmissionService) {
		this.auditSubmissionService = auditSubmissionService;
	}

	/**
	 * Process Transaction
	 */
	@Override
	public void processTransaction(Transaction transaction) {

		// Update Balance
		balance.updateAndGet(bal -> bal.add(transaction.getAmount()));

		// Add transactions to the queue
		transactionsQueue.add(transaction);

		// Check the Transaction limit and submit to Audit System
		if (transactionsQueue.size() >= transactionLimit) {
			// To ensure thread safe
			synchronized (lock) {
				if (transactionsQueue.size() >= transactionLimit) {
					List<Transaction> submissionTransList = new ArrayList<>();
					// Poll Transactions from the Queue
					for (int i = 0; i < transactionLimit; i++) {
						Optional.ofNullable(transactionsQueue.poll()).ifPresent(submissionTransList::add);
					}
					// Submit to Audit System
					if (!submissionTransList.isEmpty())
						auditSubmissionService.submit(submissionTransList);
				}
			}

		}

	}

	/**
	 * Retrieve Balance
	 */
	@Override
	public double retrieveBalance() {
		return balance.get().doubleValue();
	}

}
