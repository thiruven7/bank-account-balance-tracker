package com.bankaccount.transactionproducer.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transaction model class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

	/**
	 * Transaction ID
	 */
	private String transactionId;

	/**
	 * Credit or Debit Amount
	 */
	private BigDecimal amount;

}
