/**
 * 
 */
package com.bankaccount.balancetracker.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.entity.BalanceT;
import com.bankaccount.balancetracker.entity.TransactionT;
import com.bankaccount.balancetracker.exception.BalanceTrackerException;
import com.bankaccount.balancetracker.repository.BalanceRepository;
import com.bankaccount.balancetracker.repository.TransactionRepository;
import com.bankaccount.balancetracker.service.BankAccountService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service Implementation class for the bank account operations with DB
 * persistence
 */
@Service
@Primary
@Qualifier("dbService")
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
	/**
	 * Account ID is not required as per the requirement, internally assigned for
	 * persistence and tractability, producer does not send it.
	 */
	private static final String ACCOUNT_ID = "ACC123456";
	private final BalanceRepository balanceRepository;
	private final TransactionRepository transactionRepository;

	public BankAccountServiceImpl(BalanceRepository balanceRepository, TransactionRepository transactionRepository) {
		this.balanceRepository = balanceRepository;
		this.transactionRepository = transactionRepository;
	}

	@Override
	@Transactional
	public void processTransaction(Transaction transaction) {
		log.info("processTransaction:enter with transaction Id: {}", transaction.getTransactionId());

		// Read and update balance
		BalanceT balance = balanceRepository.findForUpdate(ACCOUNT_ID).orElseGet(() -> {
			// Initialize for the first time
			BalanceT newBalance = BalanceT.builder().accountId(ACCOUNT_ID).amount(BigDecimal.ZERO).build();
			balanceRepository.save(newBalance);
			log.info("Initialized balance row for accountId: {}", ACCOUNT_ID);
			return newBalance;
		});

		balance.setAmount(balance.getAmount().add(transaction.getAmount()));
		log.debug("Updated balance after transaction Id {}: {}", transaction.getTransactionId(), balance.getAmount());
		balanceRepository.save(balance);

		// Persist Transaction in the Transactions table
		TransactionT transactionEntity = TransactionT.builder().transactionId(transaction.getTransactionId())
				.accountId(ACCOUNT_ID).amount(transaction.getAmount()).updateDateTime(LocalDateTime.now())
				.auditStatus("PENDING").build();
		transactionRepository.save(transactionEntity);
		log.info("processTransaction:exit with transaction Id: {}", transaction.getTransactionId());
	}

	@Override
	public double retrieveBalance() {
		log.info("retrieveBalance:entry ");
		return balanceRepository.findById(ACCOUNT_ID).map(BalanceT::getAmount).map(Number::doubleValue)
				.orElseThrow(() -> new BalanceTrackerException("Balance not found for account Id " + ACCOUNT_ID,
						HttpStatus.NOT_FOUND));

	}

}
