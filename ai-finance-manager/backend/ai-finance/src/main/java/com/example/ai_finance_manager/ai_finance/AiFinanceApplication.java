package com.example.ai_finance_manager.ai_finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AiFinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiFinanceApplication.class, args);
	}

}
