package com.example.ai_finance_manager.ai_finance.alert;

import com.example.ai_finance_manager.ai_finance.Entity.User;

public interface Notifier {

    /** Metric tag value, e.g. "email" or "whatsapp". */
    String channel();

    boolean canNotify(User user);

    /** Throws on any failure so the caller can fall back to the next channel. */
    void send(AlertMessage message);
}
