package com.example.ai_finance_manager.ai_finance.alert;

import com.example.ai_finance_manager.ai_finance.Entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class WhatsAppNotifier implements Notifier {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String WA_PREFIX = "whatsapp:";

    private final RestClient restClient;
    private final boolean enabled;
    private final String sid;
    private final String from;

    public WhatsAppNotifier(@Value("${app.whatsapp.enabled:false}") boolean enabled,
                            @Value("${app.twilio.sid:}") String sid,
                            @Value("${app.twilio.token:}") String token,
                            @Value("${app.twilio.from:}") String from) {
        this.enabled = enabled;
        this.sid = sid;
        this.from = from;

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl("https://api.twilio.com")
                .requestFactory(factory)
                .defaultHeaders(h -> h.setBasicAuth(sid, token))
                .build();
    }

    @Override
    public String channel() {
        return "whatsapp";
    }

    @Override
    public boolean canNotify(User user) {
        return enabled
                && !sid.isBlank()
                && !from.isBlank()
                && user.isWhatsappOptIn()
                && user.getPhone() != null
                && !user.getPhone().isBlank();
    }

    @Override
    public void send(AlertMessage message) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", WA_PREFIX + toE164(message.user().getPhone()));
        form.add("From", from.startsWith(WA_PREFIX) ? from : WA_PREFIX + from);
        form.add("Body", buildText(message));

        restClient.post()
                .uri("/2010-04-01/Accounts/{sid}/Messages.json", sid)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }

    private String buildText(AlertMessage m) {
        StringBuilder sb = new StringBuilder()
                .append("Budget alert (").append(m.monthLabel()).append("): ")
                .append(m.statusLabel()).append(". Spent ")
                .append(AlertMessage.money(m.spent())).append(" of ")
                .append(AlertMessage.money(m.limit())).append(" (")
                .append(m.percent().toPlainString()).append("%).");
        if (m.quote() != null && !m.quote().isBlank()) {
            sb.append("\n").append(m.quote());
        }
        return sb.toString();
    }

    private String toE164(String phone) {
        String trimmed = phone.trim();
        if (trimmed.startsWith("+")) {
            return trimmed;
        }
        String digits = trimmed.replaceAll("\\D", "");
        return digits.length() == 10 ? "+91" + digits : "+" + digits;
    }
}
