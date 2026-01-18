package com.sheranga.patient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PatientApplication {

	private static final Logger logger = LoggerFactory.getLogger(PatientApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Patient Service Application...");
		try {
			SpringApplication.run(PatientApplication.class, args);
			logger.info("Patient Service Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Patient Service Application", e);
			throw e;
		}
	}

}
