package com.bankaccount.balancetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bankaccount.balancetracker.entity.TransactionT;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionT, String> {

}
