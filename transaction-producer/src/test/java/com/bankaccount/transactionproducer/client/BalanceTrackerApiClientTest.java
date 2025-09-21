/**
 * 
 */
package com.bankaccount.transactionproducer.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.bankaccount.transactionproducer.dto.Transaction;

/**
 * test cases to test BalanceTrackerApiClient operations
 */
@ExtendWith(MockitoExtension.class)
class BalanceTrackerApiClientTest {

	@InjectMocks
	private BalanceTrackerApiClient apiClient;

	@Mock
	RestTemplate restTemplate;

	@BeforeEach
	void setup() {
		ReflectionTestUtils.setField(apiClient, "baseUrl", "http://localhost:8091");
		ReflectionTestUtils.setField(apiClient, "path", "/api/bankaccount/v1/transactions");
		ReflectionTestUtils.setField(apiClient, "retryCount", 3);
		ReflectionTestUtils.setField(apiClient, "retryDelayMs", 10);
	}

	/**
	 * Verifies Send transaction to the Balance tracker API
	 */
	@Test
	void testSendTransactionWithValidTransaction() {

		// given
		Transaction transaction = Transaction.builder().transactionId("DEB123").amount(BigDecimal.valueOf(100)).build();

		when(restTemplate.postForEntity(anyString(), eq(transaction), any())).thenReturn(ResponseEntity.ok().build());
		// when
		apiClient.sendTransaction(transaction);

		verify(restTemplate, times(1)).postForEntity(anyString(), eq(transaction), any());

	}

}
