package org.example.budgeting.application;

import org.example.budgeting.application.input.PersistTransactionInput;
import org.example.budgeting.application.output.TransactionOutput;
import org.example.budgeting.domain.Transaction;
import org.example.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class PersistTransactionUseCase {
    private final TransactionRepository transactionRepository;

    public PersistTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "persist-transaction", description = "Persiste uma nova transação financeira")
    public TransactionOutput execute(PersistTransactionInput input) {

        if (input.description() == null || input.description().isBlank()) {
            throw new IllegalArgumentException("Descrição da transação não pode ser nula ou vazia");
        }

        if (input.amount() <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }
        if (input.category() == null) {
            throw new IllegalArgumentException("Categoria da transação é obrigatória");
        }

        var transaction = transactionRepository.save(
                new Transaction(input.description(), input.amount(), input.category()));

        return TransactionOutput.from(transaction);
    }
}
