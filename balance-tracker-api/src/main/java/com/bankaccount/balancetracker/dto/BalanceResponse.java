package com.bankaccount.balancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Balance response model class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Represents the current account balance")
public class BalanceResponse {

	/**
	 * Account balance
	 */
	@Schema(description = "Current balance (positive or negative)", example = "1250.75")
	private double balance;

}
