package com.project.TextToSQL;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TextToSqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(TextToSqlApplication.class, args);
	}

}
