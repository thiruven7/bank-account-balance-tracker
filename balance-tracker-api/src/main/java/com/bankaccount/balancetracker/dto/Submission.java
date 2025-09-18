package com.bankaccount.balancetracker.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class to submit the transaction details to the Audit System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonRootName("submission")
public class Submission {

	/**
	 * List of batches to be submitted to the Audit System
	 */
	List<Batch> batches;

}
