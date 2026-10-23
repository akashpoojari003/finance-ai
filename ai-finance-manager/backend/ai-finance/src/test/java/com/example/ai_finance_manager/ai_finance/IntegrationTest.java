package com.example.ai_finance_manager.ai_finance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:financeai_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test-dummy",
        "app.jwt.secret=dGVzdC1vbmx5LWR1bW15LWp3dC1zZWNyZXQtMzItYnl0ZXMtbWluLWxlbmd0aA==",
        "app.jwt.access-minutes=15",
        "app.jwt.refresh-days=7",
        "app.alerts.scheduler-enabled=false",
        "app.whatsapp.enabled=false"
})
public @interface IntegrationTest {
}