package com.bankaccount.balancetracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.BankAccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller to manage bank account operations
 * 
 */
@Tag(name = "Bank Account API", description = "APIs to manage bank account operations")
@RestController
@RequestMapping("/api/bankaccount/v1")
public class BankAccountController {

	private final BankAccountService bankAccountService;

	public BankAccountController(BankAccountService bankAccountService) {
		this.bankAccountService = bankAccountService;
	}

	/**
	 * Adds credit and debit transaction to the bank account
	 * 
	 * @param transaction
	 * @return 201 Created
	 */
	@Operation(summary = "Add Transaction", description = "Adds credit and debit transaction to the bank account")
	@ApiResponse(responseCode = "201", description = "Transaction completed successfully")
	@PostMapping("/transactions")
	public ResponseEntity<Void> addTransaction(@RequestBody Transaction transaction) {
		bankAccountService.processTransaction(transaction);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Retrieves the account balance
	 * 
	 * @return 200 Ok
	 */
	@Operation(summary = "Retrieve Balance", description = "Retrieves account balance")
	@ApiResponse(responseCode = "200", description = "Balance retrieved successfully")
	@GetMapping("/balance")
	public ResponseEntity<Double> getBalance() {
		return ResponseEntity.ok(bankAccountService.retrieveBalance());
	}

}
