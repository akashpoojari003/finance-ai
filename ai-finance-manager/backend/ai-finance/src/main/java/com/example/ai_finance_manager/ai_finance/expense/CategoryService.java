package com.example.ai_finance_manager.ai_finance.expense;


public interface CategoryService {

    String DEFAULT_CATEGORY = "Other";

    default String suggest(String note) {
        return DEFAULT_CATEGORY;
    }
}
