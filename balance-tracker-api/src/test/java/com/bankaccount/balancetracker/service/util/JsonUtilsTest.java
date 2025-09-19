package com.bankaccount.balancetracker.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bankaccount.balancetracker.dto.Transaction;

/**
 * Test class to test JsonUtils class
 */
@ExtendWith(MockitoExtension.class)
class JsonUtilsTest {


	/**
	 * Verifies Object to Json conversion with valid object
	 */
	@Test
	void testToJsonWithValidObject() {

		// given
		Transaction trans = Transaction.builder().transactionId("CRE123").amount(new BigDecimal("250.52")).build();

		// when
		String json = JsonUtils.toJson(trans);

		// then
		assertTrue(json.contains("CRE123"));
		assertTrue(json.contains("250.52"));

	}

	/**
	 * Verifies Object to Json conversion with null
	 */
	@Test
	void testToJsonWithNull() {

		// when
		String json = JsonUtils.toJson(null);

		// then
		assertEquals("null", json);

	}

}
