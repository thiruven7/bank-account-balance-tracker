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

import com.bankaccount.balancetracker.dto.Batch;
import com.bankaccount.balancetracker.dto.Submission;
import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.BankAccountService;
import com.bankaccount.balancetracker.service.helper.AuditSystemBatchBuilder;
import com.bankaccount.balancetracker.service.util.JsonUtils;

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

	@Value("${msa.auditsystem.transaction.maxAmountPerBatch}")
	private BigDecimal maxAmountPerBatch;

	private final AuditSystemBatchBuilder batchBuilder;

	public BankAccountServiceImpl(AuditSystemBatchBuilder batchBuilder) {
		this.batchBuilder = batchBuilder;
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
						submitToAuditSystem(submissionTransList);
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

	private void submitToAuditSystem(List<Transaction> transList) {
		List<Batch> batches = batchBuilder.buildBatches(transList, maxAmountPerBatch);

		Submission submission = new Submission();
		submission.setBatches(batches);
		log.info("Audit System Submission batch count = {}", submission.getBatches().size());

		// Print Audit System Submission
		log.info(JsonUtils.toJson(submission));
	}

}
