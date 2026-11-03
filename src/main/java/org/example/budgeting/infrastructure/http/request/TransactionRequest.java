package org.example.budgeting.infrastructure.http.request;

import org.example.budgeting.application.input.PersistTransactionInput;
import org.example.budgeting.domain.Category;

public record TransactionRequest(String description, Category category, long amount) {
    public PersistTransactionInput toInput() {
        return new PersistTransactionInput(description, amount, category);
    }
}
