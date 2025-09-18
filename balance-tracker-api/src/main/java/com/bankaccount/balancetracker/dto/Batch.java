package com.bankaccount.balancetracker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class to represent the Batch variables
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {
	
	/**
	 * Total value of all transactions in the batch
	 */
	private BigDecimal totalValueOfAllTransactions;
	
	/**
	 * Count of the transactions in the batch
	 */
	private int countOfTransactions;

}
