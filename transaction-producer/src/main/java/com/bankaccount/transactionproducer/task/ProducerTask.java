package com.bankaccount.transactionproducer.task;

import java.util.concurrent.TimeUnit;

import com.bankaccount.transactionproducer.client.BalanceTrackerApiClient;
import com.bankaccount.transactionproducer.dto.Transaction;
import com.bankaccount.transactionproducer.service.TransactionGeneratorService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to run the produce transaction task
 */
@AllArgsConstructor
@Slf4j
public class ProducerTask implements Runnable {

	private final TransactionGeneratorService transactionGeneratorService;
	private final BalanceTrackerApiClient balanceTrackerApiClient;
	private final boolean isCredit;
	private final int transactionsPerSecond;

	@Override
	public void run() {
		try {
			long milliSeconds = 1000 / transactionsPerSecond;
			while (!Thread.currentThread().isInterrupted()) {
				Transaction trans = isCredit ? transactionGeneratorService.generateCredit()
						: transactionGeneratorService.generateDebit();
				balanceTrackerApiClient.sendTransaction(trans);
				TimeUnit.MILLISECONDS.sleep(milliSeconds);
			}
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("Exception occured while producing transaction", e);
		}

	}

}
