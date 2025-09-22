package com.bankaccount.balancetracker.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankaccount.balancetracker.dto.BalanceResponse;
import com.bankaccount.balancetracker.dto.ErrorResponse;
import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.exception.BalanceTrackerException;
import com.bankaccount.balancetracker.service.BankAccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller to manage bank account operations
 * 
 */
@Tag(name = "Bank Account API", description = "APIs to manage bank account operations")
@RestController
@RequestMapping("/api/bankaccount/v1")
@Slf4j
public class BankAccountController {

	@Value("${msa.bank.transaction.amount.min}")
	private BigDecimal minAmount;

	@Value("${msa.bank.transaction.amount.max}")
	private BigDecimal maxAmount;

	private final BankAccountService bankAccountService;

	public BankAccountController(@Qualifier("dbService") BankAccountService bankAccountService) {
		this.bankAccountService = bankAccountService;
	}

	/**
	 * Adds credit and debit transaction to the bank account
	 * 
	 * @param transaction
	 * @return 201 Created
	 */
	// Authentication and Authorization using OAuth or JWT could be applied in
	// Production grade application.
	@Operation(summary = "Add Transaction", description = "Adds credit and debit transaction to the bank account")
	@ApiResponse(responseCode = "201", description = "Transaction completed successfully")
	@ApiResponse(responseCode = "400", description = "Invalid transaction input", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/transactions")
	public ResponseEntity<Void> addTransaction(@Valid @RequestBody Transaction transaction) {
		log.info("Received Transaction : transaction Id = {}, amount = {}, timestamp = {}",
				transaction.getTransactionId(), transaction.getAmount(), LocalDateTime.now());
		// validation for amount min & max range and signs for credit and debit
		validateTransaction(transaction);
		bankAccountService.processTransaction(transaction);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Retrieves the account balance
	 * 
	 * @return 200 Ok
	 */
	// Authentication, Authorization and Retry option on 404 could be applied in
	// Production grade application.
	@Operation(summary = "Retrieve Balance", description = "Retrieves account balance")
	@ApiResponse(responseCode = "200", description = "Balance retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BalanceResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/balance")
	public ResponseEntity<BalanceResponse> getBalance() {
		log.info("Retrive Balance : entry");
		double balance = bankAccountService.retrieveBalance();
		return ResponseEntity.ok(new BalanceResponse(balance));
	}

	/**
	 * Validation for amount
	 * 
	 * @param transaction transaction received.
	 */
	private void validateTransaction(Transaction transaction) {
		BigDecimal amount = transaction.getAmount();
		if (amount == null || amount.abs().compareTo((minAmount)) < 0 || amount.abs().compareTo(maxAmount) > 0) {
			throw new BalanceTrackerException("Amount must be between £" + minAmount + " and £" + maxAmount,
					HttpStatus.BAD_REQUEST);
		}
		if (transaction.getTransactionId().startsWith("CRE") && amount.signum() <= 0) {
			throw new BalanceTrackerException("Credit must have positive amount", HttpStatus.BAD_REQUEST);
		}
		if (transaction.getTransactionId().startsWith("DEB") && amount.signum() >= 0) {
			throw new BalanceTrackerException("Debit must have negative amount", HttpStatus.BAD_REQUEST);
		}
	}

}
