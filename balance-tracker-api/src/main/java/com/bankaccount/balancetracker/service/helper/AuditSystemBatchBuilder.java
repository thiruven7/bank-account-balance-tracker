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
		log.info("buildBatches:enter with transaction list: {} and Max Amount Per Batch = {}",
				transList != null ? transList.size() : "null", maxAmountPerBatch);

		List<Batch> batches = new ArrayList<>();

		if (transList == null || transList.isEmpty()) {
			log.info("No transactions to submit to Audit System");
			return batches;
		}

		// Sort by absolute amount descending
		List<Transaction> sortedList = transList.stream()
				.sorted((a, b) -> b.getAmount().abs().compareTo(a.getAmount().abs())).toList();
		log.info("Sorted transaction amounts - descending order : {}",
				sortedList.stream().map(t -> t.getAmount().abs()).toList());

		// Apply First Fit Decreasing to minimize batch count
		for (Transaction trans : sortedList) {
			BigDecimal absoluteAmount = trans.getAmount().abs();
			boolean isPlaced = false;

			// Try to fit into an existing batch
			for (var i = 0; i < batches.size(); i++) {
				Batch batch = batches.get(i);
				if (batch.getTotalValueOfAllTransactions().add(absoluteAmount).compareTo(maxAmountPerBatch) <= 0) {
					// Add transaction details to the batch
					batch.addTransaction(absoluteAmount);
					isPlaced = true;
					log.info("Placing transaction {} with value {} into batch {}", trans.getTransactionId(),
							absoluteAmount, i + 1);
					break;
				}
			}

			// If no batch can accommodate, create a new one
			if (!isPlaced) {
				Batch newBatch = new Batch(absoluteAmount, 1);
				batches.add(newBatch);
				log.info("Creating new batch #{} for transaction {} with value {}", batches.size(),
						trans.getTransactionId(), absoluteAmount);
			}
		}
		log.info("buildBatches:exit with batch count {}", batches.size());
		return batches;
	}

}
