package com.bankaccount.balancetracker.dto;

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
	 * 
	 */
	private String transactionId;
	
	/**
	 * 
	 */
	private BigDecimal amount;

}
