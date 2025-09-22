package com.bankaccount.balancetracker.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Represents a credit or debit transaction")
public class Transaction {

	/**
	 * Transaction ID
	 */
	@NotBlank(message = "Transaction ID must not be null or empty")
	@Schema(description = "Unique transaction ID", example = "CRI-3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private String transactionId;

	/**
	 * Credit or Debit Amount
	 */
	@NotNull(message = "Amount must not be null")
	@Schema(description = "Transaction amount (positive for credit, negative for debit)", example = "150.00")
	private BigDecimal amount;

}
