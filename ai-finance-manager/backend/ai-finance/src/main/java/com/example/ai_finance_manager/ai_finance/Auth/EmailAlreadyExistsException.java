package com.example.ai_finance_manager.ai_finance.Auth;


import com.example.ai_finance_manager.ai_finance.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
