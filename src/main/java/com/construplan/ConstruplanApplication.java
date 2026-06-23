package com.construplan;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class ConstruplanApplication {

	@PostConstruct
	public void initializeTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
	}

	
	public static void main(String[] args) {
		SpringApplication.run(ConstruplanApplication.class, args);
	}

}
