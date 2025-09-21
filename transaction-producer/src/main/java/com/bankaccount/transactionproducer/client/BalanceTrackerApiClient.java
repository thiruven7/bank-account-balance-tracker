package com.bankaccount.transactionproducer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.bankaccount.transactionproducer.dto.Transaction;

import lombok.extern.slf4j.Slf4j;

/**
 * Client class to invoke Balance Tracker APIs
 */
@Component
@Slf4j
public class BalanceTrackerApiClient {

	@Value("${msa.balance-tracker-api.url}")
	private String baseUrl;

	@Value("${msa.balance-tracker-api.path}")
	private String path;

	@Value("${msa.producer.client.retry-count}")
	private int retryCount;

	@Value("${msa.producer.client.retry-delay}")
	private int retryDelayMs;

	private final RestTemplate restTemplate;

	public BalanceTrackerApiClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;

	}

	/**
	 * Method to send the transaction to the balance tracker api.
	 * 
	 * @param transaction credit/debit transaction
	 */
	public void sendTransaction(Transaction transaction) {
		log.debug("sendTransaction: entry: transaction Id {}", transaction.getTransactionId());

		boolean isSent = false;
		int attempts = 0;

		String endpoint = baseUrl + path;
		log.debug("Sending transaction to endpoint: {}", endpoint);

		// Retry mechanism to reduce failures
		while (!isSent && attempts < retryCount) {
			try {
				restTemplate.postForEntity(endpoint, transaction, Void.class);
				isSent = true;
				log.info("Sent transaction : {}", transaction.getTransactionId());
			} catch (HttpClientErrorException e) {
				log.warn("Client error while sending transaction: transactionId = {}: status = {}, body = {}",
						transaction.getTransactionId(), e.getStatusCode(), e.getResponseBodyAsString());
				break; // No retry for 4xx error
			} catch (RestClientException re) {
				// Increment attempt count
				attempts++;
				log.warn("Attempt {} failed for transaction: transaction Id {} : {}", attempts,
						transaction.getTransactionId(), re);
				try {
					// Backoff
					log.debug("Waiting {} ms before retrying transaction Id {}", retryDelayMs,
							transaction.getTransactionId());
					Thread.sleep(retryDelayMs);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		if (!isSent) {
			log.error("Failed to send transaction after {} attempts for transaction Id: {}", attempts,
					transaction.getTransactionId());
		}
		log.debug("sendTransaction: exit: transaction Id {}", transaction.getTransactionId());

	}

}
