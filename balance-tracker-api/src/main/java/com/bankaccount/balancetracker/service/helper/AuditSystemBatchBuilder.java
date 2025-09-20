package com.bankaccount.balancetracker.service.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.bankaccount.balancetracker.dto.Batch;
import com.bankaccount.balancetracker.dto.Transaction;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class to build the batches to submit to the Audit System
 */
@Slf4j
@Component
public class AuditSystemBatchBuilder {

	/**
	 * Method to build batches
	 * 
	 * @param transList         Transactions
	 * @param maxAmountPerBatch Configured value per batch
	 * @return List of Batch
	 */
	public List<Batch> buildBatches(List<Transaction> transList, BigDecimal maxAmountPerBatch) {
		log.debug("buildBatches:enter with transaction list: {} and Max Amount Per Batch = {}",
				transList != null ? transList.size() : "null", maxAmountPerBatch);

		List<Batch> batches = new ArrayList<>();

		if (transList == null || transList.isEmpty()) {
			log.info("No transactions to submit to Audit System");
			return batches;
		}

		var batchAmount = BigDecimal.ZERO;
		var batchTransCount = 0;

		for (Transaction trans : transList) {
			BigDecimal absoluteAmount = trans.getAmount().abs();
			if (batchAmount.add(absoluteAmount).compareTo(maxAmountPerBatch) > 0) {
				batches.add(new Batch(batchAmount, batchTransCount));
				batchAmount = BigDecimal.ZERO;
				batchTransCount = 0;
			}
			batchAmount = batchAmount.add(absoluteAmount);
			batchTransCount++;
		}
		if (batchTransCount != 0) {
			batches.add(new Batch(batchAmount, batchTransCount));
		}
		log.debug("buildBatches:exit with batch count {}", batches.size());
		return batches;
	}

}
