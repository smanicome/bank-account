package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeAmountException;
import io.smanicome.bank_account.exceptions.NegativeBalanceException;
import io.smanicome.bank_account.persistence.BankClientRepository;
import io.smanicome.bank_account.persistence.BankOperationRepository;
import io.smanicome.bank_account.visual.StatementWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {
    private final Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault());

    @Mock
    private BankClientRepository bankClientRepository;

    @Mock
    private BankOperationRepository bankOperationRepository;

    @Mock
    private StatementWriter statementWriter;

    private BankService bankService;

    @BeforeEach
    void setUp() {
        bankService = new BankService(clock, bankClientRepository, bankOperationRepository, statementWriter);
    }

    @Nested
    class DepositShould {
        @Test
        void returnBankOperation() throws ClientNotFoundException, NegativeAmountException {
            final UUID operationId = UUID.randomUUID();
            final UUID clientId = UUID.randomUUID();
            final LocalDateTime date = LocalDateTime.now(clock);
            final Amount amount = Amount.of(BigDecimal.ONE);
            final String label = "test";
            final BankOperation.OperationType type = BankOperation.OperationType.DEPOSIT;

            final BankOperation lastOperation = new BankOperation(null, clientId, type, Amount.of(BigDecimal.TEN), Amount.of(BigDecimal.TEN), date, "last");
            final Amount newBalance = lastOperation.balance().add(amount);

            final BankOperation operationToSave = new BankOperation(null, clientId, type, amount, newBalance, date, label);
            final BankOperation expectedOperation = new BankOperation(operationId, clientId, type, amount, newBalance, date, label);

            when(bankClientRepository.existsById(any())).thenReturn(true);
            when(bankOperationRepository.findLatestOperationByClientId(any())).thenReturn(Optional.of(lastOperation));
            when(bankOperationRepository.save(any())).thenReturn(expectedOperation);


            final BankOperation operation = bankService.deposit(clientId, amount, label);

            assertEquals(expectedOperation, operation);

            final InOrder orderVerifier = inOrder(bankOperationRepository, bankClientRepository);
            orderVerifier.verify(bankClientRepository).existsById(clientId);
            orderVerifier.verify(bankOperationRepository).findLatestOperationByClientId(clientId);
            orderVerifier.verify(bankOperationRepository).save(operationToSave);
            orderVerifier.verifyNoMoreInteractions();
        }

        @Test
        void throwOnUnknownClient() {
            final UUID clientId = UUID.randomUUID();

            assertThrows(ClientNotFoundException.class, () -> bankService.deposit(clientId, Amount.ZERO, ""));

            verify(bankClientRepository).existsById(clientId);
            verifyNoMoreInteractions(bankClientRepository, bankOperationRepository);
        }
    }

    @Nested
    class WithdrawalShould {
        @Test
        void returnBankOperation() throws ClientNotFoundException, NegativeAmountException, NegativeBalanceException {
            final UUID operationId = UUID.randomUUID();
            final UUID clientId = UUID.randomUUID();
            final LocalDateTime date = LocalDateTime.now(clock);
            final Amount amount = Amount.of(BigDecimal.ONE);
            final BankOperation.OperationType type = BankOperation.OperationType.WITHDRAWAL;

            final BankOperation lastOperation = new BankOperation(null, clientId, type, Amount.of(BigDecimal.TEN), Amount.of(BigDecimal.TEN), date, "last");
            final Amount newBalance = lastOperation.balance().subtract(amount);

            final BankOperation operationToSave = new BankOperation(null, clientId, type, amount, newBalance, date, "withdrawal");
            final BankOperation expectedOperation = new BankOperation(operationId, clientId, type, amount, newBalance, date, "withdrawal");

            when(bankClientRepository.existsById(any())).thenReturn(true);
            when(bankOperationRepository.findLatestOperationByClientId(any())).thenReturn(Optional.of(lastOperation));
            when(bankOperationRepository.save(any())).thenReturn(expectedOperation);


            final BankOperation operation = bankService.withdraw(clientId, amount);

            assertEquals(expectedOperation, operation);

            final InOrder orderVerifier = inOrder(bankOperationRepository, bankClientRepository);
            orderVerifier.verify(bankClientRepository).existsById(clientId);
            orderVerifier.verify(bankOperationRepository).findLatestOperationByClientId(clientId);
            orderVerifier.verify(bankOperationRepository).save(operationToSave);
            orderVerifier.verifyNoMoreInteractions();
        }

        @Test
        void throwOnUnknownClient() {
            final UUID clientId = UUID.randomUUID();

            assertThrows(ClientNotFoundException.class, () -> bankService.withdraw(clientId, Amount.ZERO));

            verify(bankClientRepository).existsById(clientId);
            verifyNoMoreInteractions(bankClientRepository, bankOperationRepository);
        }

        @Test
        void throwOnNegativeBalance() throws NegativeAmountException {
            final UUID clientId = UUID.randomUUID();
            final LocalDateTime date = LocalDateTime.now(clock);
            final Amount amount = Amount.of(BigDecimal.ONE);
            final BankOperation.OperationType type = BankOperation.OperationType.WITHDRAWAL;

            final BankOperation lastOperation = new BankOperation(null, clientId, type, Amount.ZERO, Amount.ZERO, date, "last");

            when(bankClientRepository.existsById(any())).thenReturn(true);
            when(bankOperationRepository.findLatestOperationByClientId(any())).thenReturn(Optional.of(lastOperation));


            assertThrows(NegativeBalanceException.class, () -> bankService.withdraw(clientId, amount));

            final InOrder orderVerifier = inOrder(bankOperationRepository, bankClientRepository);
            orderVerifier.verify(bankClientRepository).existsById(clientId);
            orderVerifier.verify(bankOperationRepository).findLatestOperationByClientId(clientId);
            orderVerifier.verifyNoMoreInteractions();
        }
    }

    @Nested
    class StatementPrintingShould {
        @Test
        void shouldPrintStatement() throws NegativeAmountException, ClientNotFoundException {
            final UUID clientId = UUID.randomUUID();
            final LocalDateTime date = LocalDateTime.now(clock);

            final var operations = List.of(
                    new BankOperation(
                            UUID.randomUUID(),
                            clientId,
                            BankOperation.OperationType.WITHDRAWAL,
                            Amount.of(BigDecimal.valueOf(1000)),
                            Amount.of(BigDecimal.valueOf(6000)),
                            date.plusMinutes(30),
                            "withdrawal"
                    ),
                    new BankOperation(
                            UUID.randomUUID(),
                            clientId,
                            BankOperation.OperationType.DEPOSIT,
                            Amount.of(BigDecimal.valueOf(5000)),
                            Amount.of(BigDecimal.valueOf(7000)),
                            date.plusMinutes(20),
                            "deposit 2"
                    ),
                    new BankOperation(
                            UUID.randomUUID(),
                            clientId,
                            BankOperation.OperationType.DEPOSIT,
                            Amount.of(BigDecimal.valueOf(2000)),
                            Amount.of(BigDecimal.valueOf(2000)),
                            date.plusMinutes(10),
                            "deposit 1"
                    )
            );

            final Statement statement = new Statement(clientId, operations, date);

            when(bankClientRepository.existsById(any())).thenReturn(true);
            when(bankOperationRepository.findByAccountId(any())).thenReturn(operations);

            bankService.printAccountStatement(clientId);

            final var orderVerifier = inOrder(bankClientRepository, bankOperationRepository, statementWriter);
            orderVerifier.verify(bankClientRepository).existsById(clientId);
            orderVerifier.verify(bankOperationRepository).findByAccountId(clientId);
            orderVerifier.verify(statementWriter).write(statement);
            orderVerifier.verifyNoMoreInteractions();
        }

        @Test
        void throwOnUnknownClient() {
            final UUID clientId = UUID.randomUUID();

            assertThrows(ClientNotFoundException.class, () -> bankService.printAccountStatement(clientId));

            verify(bankClientRepository).existsById(clientId);
            verifyNoMoreInteractions(bankClientRepository, bankOperationRepository);
        }
    }
}