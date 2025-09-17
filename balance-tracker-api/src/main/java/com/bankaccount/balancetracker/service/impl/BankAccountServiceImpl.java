package com.bankaccount.balancetracker.service.impl;

import org.springframework.stereotype.Service;

import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.service.BankAccountService;

/**
 * Service Implementation class for the bank account operations
 */
@Service
public class BankAccountServiceImpl implements BankAccountService {

	@Override
	public void processTransaction(Transaction transaction) {
		// TODO Auto-generated method stub

	}

	@Override
	public double retrieveBalance() {
		// TODO Auto-generated method stub
		return 0;
	}

}
