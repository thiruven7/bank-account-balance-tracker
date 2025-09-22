package com.bankaccount.transactionproducer.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bankaccount.transactionproducer.client.BalanceTrackerApiClient;
import com.bankaccount.transactionproducer.exception.TransactionProducerException;
import com.bankaccount.transactionproducer.task.ProducerTask;

import lombok.extern.slf4j.Slf4j;

/**
 * Producer Service to execute the credit and debit transactions
 */
@Service
@Slf4j
public class TransactionProducerService {

	private final TransactionGeneratorService transactionGeneratorService;
	private final BalanceTrackerApiClient balanceTrackerApiClient;
	private boolean isRunning = false;

	private ExecutorService executorService;

	@Value("${msa.producer.threadpoolcount}")
	private int threadPoolCount;

	@Value("${msa.producer.rate.credits-per-sec}")
	private int creditsPerSec;

	@Value("${msa.producer.rate.debits-per-sec}")
	private int debitsPerSec;

	public TransactionProducerService(TransactionGeneratorService transactionGeneratorService,
			BalanceTrackerApiClient balanceTrackerApiClient) {
		this.transactionGeneratorService = transactionGeneratorService;
		this.balanceTrackerApiClient = balanceTrackerApiClient;
	}

	/**
	 * Start producing transactions
	 */
	public synchronized void startProducing() {

		if (isRunning) {
			throw new TransactionProducerException("Producer is already running", HttpStatus.CONFLICT);
		}

		executorService = Executors.newFixedThreadPool(threadPoolCount);
		// Credit Transactions Thread
		executorService
				.submit(new ProducerTask(transactionGeneratorService, balanceTrackerApiClient, true, creditsPerSec));
		// Debit Transactions Thread
		executorService
				.submit(new ProducerTask(transactionGeneratorService, balanceTrackerApiClient, false, debitsPerSec));

		isRunning = true;
		log.info("Producer started");
	}

	/**
	 * Stop producing transactions
	 */
	public synchronized void shutdown() {
		if (!isRunning || executorService == null) {
			throw new TransactionProducerException("Producer is not running", HttpStatus.NOT_FOUND);
		}
		executorService.shutdownNow();
		executorService = null;
		isRunning = false;
		log.info("Producer stopped");

	}

	/**
	 * Method to confirm status
	 * 
	 * @return true/false
	 */
	public boolean isRunning() {
		return isRunning;
	}
}
