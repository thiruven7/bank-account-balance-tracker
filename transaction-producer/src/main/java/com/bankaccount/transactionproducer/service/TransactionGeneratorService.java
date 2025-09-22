package com.bankaccount.transactionproducer.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bankaccount.transactionproducer.dto.Transaction;

/**
 * Class to Generate the Random Credit and Debit Transactions
 */
@Service
public class TransactionGeneratorService {

	@Value("${msa.producer.amount.min}")
	private BigDecimal minAmount;

	@Value("${msa.producer.amount.max}")
	private BigDecimal maxAmount;

	/**
	 * Method to generate credit transaction
	 * 
	 * @return Transaction object
	 */
	public Transaction generateCredit() {
		return new Transaction("CRE-".concat(UUID.randomUUID().toString()), randomAmount(true));
	}

	/**
	 * Method to generate debit transaction
	 * 
	 * @return Transaction object
	 */
	public Transaction generateDebit() {
		return new Transaction("DEB-".concat(UUID.randomUUID().toString()), randomAmount(false));
	}

	/**
	 * Method to Generate random amount
	 * 
	 * @param isCredit true/false
	 * @return amount
	 */
	private BigDecimal randomAmount(boolean isCredit) {
		if (minAmount.compareTo(maxAmount) == 0) {
			BigDecimal fixedValue = minAmount.setScale(2, RoundingMode.HALF_UP);
			return isCredit ? fixedValue : fixedValue.negate();
		}
		double value = ThreadLocalRandom.current().nextDouble(minAmount.doubleValue(), maxAmount.doubleValue());
		BigDecimal randomValue = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
		return isCredit ? randomValue : randomValue.negate();
	}

}
