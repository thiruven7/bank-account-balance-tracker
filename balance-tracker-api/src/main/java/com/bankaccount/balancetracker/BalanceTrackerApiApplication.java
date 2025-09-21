package com.bankaccount.balancetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Balance Tracker API Application
 */
@EnableScheduling
@SpringBootApplication
public class BalanceTrackerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BalanceTrackerApiApplication.class, args);
	}

}
