/**
 * 
 */
package com.bankaccount.transactionproducer.integeration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.bankaccount.transactionproducer.dto.ErrorResponse;

/**
 * Integration test cases for Transaction Producer
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
class TransactionProducer_IT {

	@Autowired
	TestRestTemplate testRestTemplate;

	@AfterEach
	void tearDown() {
		// Attempt to stop producer to clean up,ignore 404
		testRestTemplate.postForEntity("/api/producer/v1/stop", null, String.class);
	}

	@Test
	void testStartProducer() {
		// when
		ResponseEntity<String> response = testRestTemplate.postForEntity("/api/producer/v1/start", null, String.class);
		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo("Producer started");
	}

	@Test
	void testStartProducerWhenAlreadyRunning() {
		// given
		testRestTemplate.postForEntity("/api/producer/v1/start", null, String.class);
		// when
		ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity("/api/producer/v1/start", null,
				ErrorResponse.class);
		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertNotNull(response.getBody());
		assertEquals(409, response.getBody().getStatus());
		assertNotNull(response.getBody().getTimestamp());
		assertEquals("Producer is already running", response.getBody().getMessage());
	}

	@Test
	void testStopProducer() {
		// given
		// ensure it's running
		testRestTemplate.postForEntity("/api/producer/v1/start", null, String.class);
		// when
		ResponseEntity<String> response = testRestTemplate.postForEntity("/api/producer/v1/stop", null, String.class);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo("Producer stopped");
	}

	@Test
	void testStopProducerWhenNotRunning() {
		// when
		ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity("/api/producer/v1/stop", null,
				ErrorResponse.class);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertNotNull(response.getBody());
		assertEquals(404, response.getBody().getStatus());
		assertNotNull(response.getBody().getTimestamp());
		assertEquals("Producer is not running", response.getBody().getMessage());
	}

	@Test
	void testStatusWhenRuuning() {
		// given
		testRestTemplate.postForEntity("/api/producer/v1/start", null, String.class);
		// when
		ResponseEntity<String> response = testRestTemplate.getForEntity("/api/producer/v1/status", String.class);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("RUNNING");
	}

	@Test
	void testStatusWhenStopped() {
		// when
		ResponseEntity<String> response = testRestTemplate.getForEntity("/api/producer/v1/status", String.class);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("STOPPED");
	}

}
