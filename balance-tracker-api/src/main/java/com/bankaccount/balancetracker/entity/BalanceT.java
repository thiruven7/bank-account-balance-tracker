package com.bankaccount.balancetracker.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "balance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceT {
	@Id
	private String accountId;

	@Column(nullable = false)
	private BigDecimal amount;

}
