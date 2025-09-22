/**
 * 
 */
package com.bankaccount.balancetracker.service.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.bankaccount.balancetracker.dto.Submission;

import lombok.extern.slf4j.Slf4j;

/**
 * AuditLogger to write the submission object to the file for demo purpose
 */
@Slf4j
public class AuditLogWriter {

	private static final String LOG_DIR = "src/main/resources/audit-logs";

	private AuditLogWriter() {

	}

	/**
	 * Method to write submission to a json file
	 * 
	 * @param submission submission to the audit system
	 */
	public static void writeSubmissionLog(Submission submission) {
		log.debug("Audit submission written to file: entry");
		try {
			Files.createDirectories(Paths.get(LOG_DIR));

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String filename = String.format("audit_submission_%s.json", timestamp);
			Path filePath = Paths.get(LOG_DIR, filename);

			String json = JsonUtils.toJson(submission);
			Files.writeString(filePath, json, StandardOpenOption.CREATE);

			log.info("Audit submission written to file: {} at {}", filePath.toAbsolutePath(), LocalDateTime.now());
		} catch (IOException e) {
			log.error("Failed to write audit submission log", e);
		}
		log.debug("Audit submission written to file: exit");
	}

}
