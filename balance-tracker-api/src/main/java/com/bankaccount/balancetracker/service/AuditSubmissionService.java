/**
 * 
 */
package com.bankaccount.balancetracker.service;

import java.util.List;

import com.bankaccount.balancetracker.dto.Transaction;

/**
 * Service to submit transactions to the Audit System
 */
public interface AuditSubmissionService {

	/**
	 * Submit transactions to the Audit System
	 * 
	 * @param transactions transactions to submit to the Audit System
	 */
	void submit(List<Transaction> transactions);

}
