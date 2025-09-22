package com.bankaccount.balancetracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bankaccount.balancetracker.entity.TransactionT;

import jakarta.persistence.LockModeType;

/**
 * Repository interface for accessing transaction records.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionT, String> {

	/**
	 * Fetches pending transactions ordered by oldest first. Applied pessimistic
	 * locking to prevent lost updates under concurrent transaction load. In
	 * production (e.g., PostgreSQL), this translates to: SELECT ... FOR UPDATE —
	 * locking the row until commit. H2 supports this syntax for testing but does
	 * not enforce true row level locks.
	 * 
	 * @param pageable pagination and limit config
	 * @return a page of pending transactions
	 */
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t from TransactionT t WHERE t.auditStatus = 'PENDING' ORDER BY t.updatedDateTime ASC")
	Page<TransactionT> findPendingTransactions(Pageable pageable);

}
