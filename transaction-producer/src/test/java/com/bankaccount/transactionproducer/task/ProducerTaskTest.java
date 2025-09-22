package com.bankaccount.transactionproducer.task;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bankaccount.transactionproducer.client.BalanceTrackerApiClient;
import com.bankaccount.transactionproducer.dto.Transaction;
import com.bankaccount.transactionproducer.service.TransactionGeneratorService;

/**
 * Class to test Producer Task
 */
@ExtendWith(MockitoExtension.class)
class ProducerTaskTest {

	@Mock
	private TransactionGeneratorService transactionGeneratorService;

	@Mock
	private BalanceTrackerApiClient balanceTrackerApiClient;

	@Test
	void testProduceCreditTransactions() throws InterruptedException {
		// given
		Transaction trans = Transaction.builder().transactionId("CRI-1234").amount(new BigDecimal("200")).build();
		ProducerTask task = new ProducerTask(transactionGeneratorService, balanceTrackerApiClient, true, 2);
		when(transactionGeneratorService.generateCredit()).thenReturn(trans);

		// when
		Thread thread = new Thread(task);
		thread.start();
		Thread.sleep(300);

		// then
		verify(transactionGeneratorService, atLeastOnce()).generateCredit();
		verify(balanceTrackerApiClient, atLeastOnce()).sendTransaction(trans);
		thread.interrupt();
	}

	@Test
	void testProduceDebitTransactions() throws InterruptedException {
		// given
		Transaction trans = Transaction.builder().transactionId("DEB-1234").amount(new BigDecimal("200")).build();
		ProducerTask task = new ProducerTask(transactionGeneratorService, balanceTrackerApiClient, false, 2);
		when(transactionGeneratorService.generateDebit()).thenReturn(trans);

		// when
		Thread thread = new Thread(task);
		thread.start();
		Thread.sleep(300);

		// then
		verify(transactionGeneratorService, atLeastOnce()).generateDebit();
		verify(balanceTrackerApiClient, atLeastOnce()).sendTransaction(trans);
		thread.interrupt();
	}

	@Test
	void testUnexpectedExceptionHandle() throws InterruptedException {
		// given
		ProducerTask task = new ProducerTask(transactionGeneratorService, balanceTrackerApiClient, false, 2);
		when(transactionGeneratorService.generateDebit()).thenThrow(new RuntimeException("Unexpected failure"));

		// when
		Thread thread = new Thread(task);
		thread.start();
		Thread.sleep(300);

		// then
		verify(transactionGeneratorService, atLeastOnce()).generateDebit();
		thread.interrupt();
	}

}
