package com.bankaccount.balancetracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.bankaccount.balancetracker.dto.Transaction;
import com.bankaccount.balancetracker.entity.BalanceT;
import com.bankaccount.balancetracker.exception.BalanceTrackerException;
import com.bankaccount.balancetracker.repository.BalanceRepository;
import com.bankaccount.balancetracker.repository.TransactionRepository;

/**
 * Test class to test BankAccountService implementation with DB integration.
 */
@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

	private static final String ACCOUNT_ID = "ACC123456";

	@InjectMocks
	private BankAccountServiceImpl bankAccountServiceImpl;

	@Mock
	private BalanceRepository balanceRepository;

	@Mock
	private TransactionRepository transactionRepository;

	/**
	 * Verifies balance and transaction persistance
	 */
	@Test
	void testProcessTransactionWithExistingBalance() {

		// given
		Transaction trans = Transaction.builder().transactionId("CRE123").amount(new BigDecimal("250.52")).build();

		BalanceT balanceT = BalanceT.builder().accountId(ACCOUNT_ID).amount(new BigDecimal("10")).build();

		when(balanceRepository.findForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(balanceT));

		// when
		bankAccountServiceImpl.processTransaction(trans);

		// then
		verify(balanceRepository, times(1)).save(any());
		verify(transactionRepository, times(1)).save(any());
	}

	@Test
	void testProcessTransactionWithNoBalance() {

		// given
		Transaction trans = Transaction.builder().transactionId("CRE123").amount(new BigDecimal("250.52")).build();

		when(balanceRepository.findForUpdate(ACCOUNT_ID)).thenReturn(Optional.empty());

		// when
		bankAccountServiceImpl.processTransaction(trans);

		// then
		verify(balanceRepository, times(2)).save(any());
		verify(transactionRepository, times(1)).save(any());
	}

	/**
	 * Verifies the retrieve balance is Zero when no transaction made.
	 */
	@Test
	void testRetrieveBalanceWithZeroBalance() {
		// when
		when(balanceRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());
		// then
		BalanceTrackerException ex = assertThrows(BalanceTrackerException.class,
				() -> bankAccountServiceImpl.retrieveBalance());
		assertEquals("Balance not found for account Id ACC123456", ex.getMessage());
		assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
	}

	/**
	 * Verifies the retrieve balance when balance exist in the table
	 */
	@Test
	void testRetrieveBalanceWithBalance() {
		// given
		BalanceT balanceT = BalanceT.builder().accountId(ACCOUNT_ID).amount(new BigDecimal("100")).build();
		when(balanceRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(balanceT));
		// when
		double balance = bankAccountServiceImpl.retrieveBalance();
		// then
		assertEquals(100.0, balance, 0.001);
	}

}
