/**
 * 
 */
package com.bankaccount.balancetracker.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bankaccount.balancetracker.dto.Batch;
import com.bankaccount.balancetracker.dto.Submission;
import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.AuditSubmissionService;
import com.bankaccount.balancetracker.service.helper.AuditSystemBatchBuilder;
import com.bankaccount.balancetracker.service.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation to submit transactions to the Audit System
 */
@Service
@Slf4j
public class AuditSubmissionServiceImpl implements AuditSubmissionService {

	private final AuditSystemBatchBuilder batchBuilder;

	@Value("${msa.auditsystem.transaction.maxAmountPerBatch}")
	private BigDecimal maxAmountPerBatch;

	public AuditSubmissionServiceImpl(AuditSystemBatchBuilder batchBuilder) {
		this.batchBuilder = batchBuilder;
	}

	/**
	 * Submit transactions to the Audit System
	 * 
	 * @param transactions transactions to submit to the Audit System
	 */

	@Override
	public void submit(List<Transaction> transactions) {
		List<Batch> batches = batchBuilder.buildBatches(transactions, maxAmountPerBatch);

		Submission submission = new Submission();
		submission.setBatches(batches);
		log.info("Audit System Submission batch count = {}", submission.getBatches().size());

		// Print Audit System Submission
		log.info(JsonUtils.toJson(submission));
	}

}
