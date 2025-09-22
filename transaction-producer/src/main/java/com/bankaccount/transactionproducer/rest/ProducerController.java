package com.bankaccount.transactionproducer.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankaccount.transactionproducer.dto.ErrorResponse;
import com.bankaccount.transactionproducer.service.TransactionProducerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST APIs to control producing transactions
 */
@Tag(name = "Transaction Producer API", description = "Endpoints to control the lifecycle of the transaction producer")
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
	@Operation(summary = "Start Transaction Producer", description = "Initiates the transaction producer. It begins generating credit and debit transactions at the configured rate and sends them to the Balance Tracker API.")
	@ApiResponse(responseCode = "200", description = "Producer started")
	@ApiResponse(responseCode = "409", description = "Producer is already running", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/start")
	public ResponseEntity<String> start() {
		producerService.startProducing();
		return ResponseEntity.ok("Producer started");
	}

	/**
	 * API to stop the producer 200 Ok
	 */
	@Operation(summary = "Stop Transaction Producer", description = "Terminates the transaction producer if it is currently running.")
	@ApiResponse(responseCode = "200", description = "Producer stopped")
	@ApiResponse(responseCode = "404", description = "Producer is not running", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/stop")
	public ResponseEntity<String> stop() {
		producerService.shutdown();
		return ResponseEntity.ok("Producer stopped");
	}

	/**
	 * API to check the producer status
	 */
	@Operation(summary = "Check Producer Status", description = "Returns the current status of the transaction producer: RUNNING or STOPPED.")
	@ApiResponse(responseCode = "200", description = "Producer status RUNNING/STOPPED")
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/status")
	public ResponseEntity<String> status() {
		return ResponseEntity.ok().body(producerService.isRunning() ? "RUNNING" : "STOPPED");
	}

}
