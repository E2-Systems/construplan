package com.construplan;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class ConstruplanApplication {

	public static void main(String[] args) {
		// Establecemos la zona horaria de forma global antes de iniciar el contexto de Spring.
		// Esto asegura que Hibernate y la conexión JDBC utilicen la zona horaria correcta desde el arranque.
		TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
	
		SpringApplication.run(ConstruplanApplication.class, args);
	}

}
