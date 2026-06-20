package com.mediconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Main entry point for the MediConnect application.
 * This class uses Spring Boot to bootstrap the application.
 */
@SpringBootApplication
@EntityScan(basePackages = "com.mediconnect.model")
public class BackendApplication {

	/**
	 * Launches the Spring application context.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
