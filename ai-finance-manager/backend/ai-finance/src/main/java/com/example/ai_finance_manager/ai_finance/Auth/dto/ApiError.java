package com.example.ai_finance_manager.ai_finance.Auth.dto;


public record ApiError(int status, String error, String message) {
}