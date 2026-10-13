package com.example.ai_finance_manager.ai_finance.alert;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final List<Notifier> notifiers;
    private final MeterRegistry meterRegistry;

    /** WhatsApp is tried first, email is the fallback. */
    public NotificationService(WhatsAppNotifier whatsAppNotifier,
                               EmailNotifier emailNotifier,
                               MeterRegistry meterRegistry) {
        this.notifiers = List.of(whatsAppNotifier, emailNotifier);
        this.meterRegistry = meterRegistry;
    }

    /** Returns true if any channel delivered the alert. */
    public boolean notify(AlertMessage message) {
        for (Notifier notifier : notifiers) {
            if (!notifier.canNotify(message.user())) {
                continue;
            }
            try {
                notifier.send(message);
                meterRegistry.counter("alerts.sent", "channel", notifier.channel()).increment();
                return true;
            } catch (Exception ex) {
                log.warn("Alert via {} failed for user {}: {}",
                        notifier.channel(), message.user().getId(), ex.getClass().getSimpleName());
            }
        }
        return false;
    }
}
