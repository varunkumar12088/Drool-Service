package com.academy.drool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.academy")
@EnableMongoRepositories(basePackages = "com.academy")
public class DroolServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DroolServiceApplication.class, args);
	}


	@Bean
	public ObjectMapper ObjectMapper(){
		return new ObjectMapper();
	}
}
