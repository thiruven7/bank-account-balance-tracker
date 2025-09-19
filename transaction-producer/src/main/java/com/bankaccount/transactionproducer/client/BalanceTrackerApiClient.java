package com.bankaccount.transactionproducer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
	private String url;

	@Value("${msa.balance-tracker-api.path}")
	private String path;

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

		try {
			String endpoint = url + path;
			restTemplate.postForEntity(endpoint, transaction, Void.class);
			log.info("Sent transaction : {}", transaction.getTransactionId());
		} catch (RestClientException re) {
			log.error("Failed to send transaction: {}", transaction, re);
		} catch (Exception e) {
			log.error("Failed to send transaction: {}", transaction, e);
		}

	}

}
