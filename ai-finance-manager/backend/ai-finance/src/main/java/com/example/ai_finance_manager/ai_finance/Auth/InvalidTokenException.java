package com.example.ai_finance_manager.ai_finance.Auth;


public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}