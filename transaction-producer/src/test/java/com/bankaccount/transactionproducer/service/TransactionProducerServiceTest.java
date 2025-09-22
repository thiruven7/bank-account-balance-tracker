/**
 * 
 */
package com.bankaccount.transactionproducer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.bankaccount.transactionproducer.client.BalanceTrackerApiClient;
import com.bankaccount.transactionproducer.exception.TransactionProducerException;

/**
 * Test class for TransactionProducerService
 */
@ExtendWith(MockitoExtension.class)
class TransactionProducerServiceTest {

	@InjectMocks
	TransactionProducerService transactionProducerService;

	@Mock
	private TransactionGeneratorService generatorService;

	@Mock
	private BalanceTrackerApiClient balanceTrackerApiClient;

	@BeforeEach
	void setup() {

		ReflectionTestUtils.setField(transactionProducerService, "threadPoolCount", 2);
		ReflectionTestUtils.setField(transactionProducerService, "creditsPerSec", 1);
		ReflectionTestUtils.setField(transactionProducerService, "debitsPerSec", 1);
	}

	/**
	 * Verifies start producer method
	 */
	@Test
	void testStartProducer() {
		// when
		transactionProducerService.startProducing();
		// then
		assertTrue(transactionProducerService.isRunning());

		// cleanup
		transactionProducerService.shutdown();
	}

	/**
	 * Verifies start producer with duplicate start
	 */
	@Test
	void testConflictOnDuplicateStart() {
		// given
		transactionProducerService.startProducing();

		// when
		TransactionProducerException tpe = assertThrows(TransactionProducerException.class,
				() -> transactionProducerService.startProducing());

		// then
		assertEquals(HttpStatus.CONFLICT, tpe.getStatus());
		assertEquals("Producer is already running", tpe.getMessage());

		// cleanup
		transactionProducerService.shutdown();
	}

	/**
	 * Verifies stop producer method when producer not running
	 */
	@Test
	void testStopProducerWhenNotRunning() {
		// when
		TransactionProducerException tpe = assertThrows(TransactionProducerException.class,
				() -> transactionProducerService.shutdown());
		// then
		assertEquals(HttpStatus.NOT_FOUND, tpe.getStatus());
		assertEquals("Producer is not running", tpe.getMessage());
	}
}
