package com.example.ai_finance_manager.ai_finance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "app.alerts.scheduler-enabled=false")
@ActiveProfiles("test")
class AiFinanceApplicationTests {

	@Test
	void contextLoads() {
	}
}
