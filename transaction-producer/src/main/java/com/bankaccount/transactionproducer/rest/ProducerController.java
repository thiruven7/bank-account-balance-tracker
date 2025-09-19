package com.bankaccount.transactionproducer.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankaccount.transactionproducer.service.TransactionProducerService;

/**
 * REST APIs to control producing transactions
 */
@RestController
@RequestMapping("/api/producer/v1")
public class ProducerController {

	private final TransactionProducerService producerService;

	public ProducerController(TransactionProducerService producerService) {
		this.producerService = producerService;
	}

	/**
	 * API to start the producer
	 * 
	 * @return 200 Ok
	 */
	@PostMapping("/start")
	public ResponseEntity<String> start() {
		producerService.startProducing();
		return ResponseEntity.ok("Producer started");
	}

	/**
	 * API to stop the producer 200 Ok
	 */
	@PostMapping("/stop")
	public ResponseEntity<String> stop() {
		producerService.shutdown();
		return ResponseEntity.ok("Producer stopped");
	}

	/**
	 * API to check the producer status
	 */
	@GetMapping("/status")
	public ResponseEntity<String> status() {
		return ResponseEntity.ok().body(producerService.isRunning() ? "RUNNING" : "STOPPED");
	}

}
