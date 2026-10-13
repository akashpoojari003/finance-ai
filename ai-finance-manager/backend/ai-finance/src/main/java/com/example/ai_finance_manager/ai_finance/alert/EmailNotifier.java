package com.example.ai_finance_manager.ai_finance.alert;

import com.example.ai_finance_manager.ai_finance.Entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;

@Component
public class EmailNotifier implements Notifier {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotifier(JavaMailSender mailSender,
                         @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public String channel() {
        return "email";
    }

    @Override
    public boolean canNotify(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank();
    }

    @Override
    public void send(AlertMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(message.user().getEmail());
            helper.setSubject(subject(message));
            helper.setText(buildHtml(message), true);
            mailSender.send(mime);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Failed to build alert email", ex);
        }
    }

    private String subject(AlertMessage m) {
        return m.level() == com.example.ai_finance_manager.ai_finance.enums.AlertLevel.OVER
                ? "Budget exceeded for " + m.monthLabel()
                : "Budget alert: " + m.percent().setScale(0, java.math.RoundingMode.HALF_UP)
                + "% used for " + m.monthLabel();
    }

    private String buildHtml(AlertMessage m) {
        BigDecimal barWidth = m.percent().min(BigDecimal.valueOf(100)).setScale(0, java.math.RoundingMode.HALF_UP);
        String quoteBlock = (m.quote() == null || m.quote().isBlank())
                ? ""
                : "<p style=\"font-style:italic;color:#374151;margin-top:16px\">"
                + HtmlUtils.htmlEscape(m.quote()) + "</p>";

        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:16px">
                  <h2 style="margin:0 0 8px">Hi %s,</h2>
                  <p style="margin:0 0 16px">Your %s budget status:</p>
                  <div style="border:1px solid #e5e7eb;border-radius:8px;padding:16px">
                    <p style="margin:0;font-size:14px;color:#6b7280">Status</p>
                    <p style="margin:0 0 12px;font-size:20px;font-weight:bold;color:%s">%s</p>
                    <div style="background:#e5e7eb;border-radius:4px;height:8px">
                      <div style="background:%s;width:%s%%;height:8px;border-radius:4px"></div>
                    </div>
                    <p style="margin:12px 0 0">Spent <b>%s</b> of <b>%s</b> (%s%%)</p>
                  </div>
                  %s
                </div>
                """.formatted(
                HtmlUtils.htmlEscape(m.user().getName()),
                HtmlUtils.htmlEscape(m.monthLabel()),
                m.statusColor(),
                m.statusLabel(),
                m.statusColor(),
                barWidth.toPlainString(),
                AlertMessage.money(m.spent()),
                AlertMessage.money(m.limit()),
                m.percent().toPlainString(),
                quoteBlock);
    }
}