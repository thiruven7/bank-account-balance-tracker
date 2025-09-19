package com.bankaccount.balancetracker.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@NotBlank(message = "Transaction ID must not be null or empty")
	private String transactionId;
	
	/**
	 * Credit or Debit Amount
	 */
	@NotNull (message = "Amount must not be null")
	private BigDecimal amount;

}
