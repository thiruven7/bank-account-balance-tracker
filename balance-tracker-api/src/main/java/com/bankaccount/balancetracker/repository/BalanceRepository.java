package com.bankaccount.balancetracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bankaccount.balancetracker.entity.BalanceT;

import jakarta.persistence.LockModeType;

@Repository
public interface BalanceRepository extends JpaRepository<BalanceT, String> {
	/**
	 * Applied pessimistic locking to prevent lost updates under concurrent
	 * transaction load. In production (e.g., PostgreSQL), this translates to:
	 * SELECT ... FOR UPDATE — locking the row until commit. H2 supports this syntax
	 * for testing but does not enforce true row level locks.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("Select b From BalanceT b where b.accountId = :accountId")
	Optional<BalanceT> findForUpdate(@Param("accountId") String accountId);

}
