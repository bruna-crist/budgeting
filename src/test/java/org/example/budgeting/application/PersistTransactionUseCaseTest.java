package org.example.budgeting.application;

import org.example.budgeting.application.input.PersistTransactionInput;
import org.example.budgeting.domain.Category;
import org.example.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersistTransactionUseCaseTest {

    private TransactionRepository repository;
    private PersistTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        useCase = new PersistTransactionUseCase(repository);
    }

    @Test
    void shouldNotSaveTransactionWithNegativeAmount() {

        var input = new PersistTransactionInput(
                "Compras no supermercado",
                -150,
                Category.GROCERIES
        );

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(input)
        );

        assertEquals(
                "Valor da transação deve ser maior que zero",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldNotSaveTransactionWithEmptyDescription() {

        var input = new PersistTransactionInput(
                "",
                150,
                Category.GROCERIES
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(input)
        );

        verify(repository, never()).save(any());
    }
}