package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeAmountException;
import io.smanicome.bank_account.exceptions.NegativeBalanceException;
import io.smanicome.bank_account.persistence.BankClientRepository;
import io.smanicome.bank_account.persistence.BankOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceShould {
    private final Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault());

    @Mock
    private BankClientRepository bankClientRepository;

    @Mock
    private BankOperationRepository bankOperationRepository;

    private BankService bankService;

    @BeforeEach
    void setUp() {
        bankService = new BankService(bankClientRepository, bankOperationRepository, clock);
    }

    @Nested
    class Deposit {
        @Test
        void returnBankOperation() throws ClientNotFoundException, NegativeAmountException {
            final UUID operationId = UUID.randomUUID();
            final UUID clientId = UUID.randomUUID();
            final LocalDate date = LocalDate.now(clock);
            final Amount amount = Amount.of(BigInteger.ONE);
            final String label = "test";
            final BankOperation.OperationType type = BankOperation.OperationType.DEPOSIT;

            final BankOperation lastOperation = new BankOperation(null, clientId, type, Amount.of(BigInteger.TEN), Amount.of(BigInteger.TEN), date, "last");
            final Amount newBalance = lastOperation.balance().add(amount);

            final BankOperation operationToSave = new BankOperation(null, clientId, type, amount, newBalance, date, label);
            final BankOperation expectedOperation = new BankOperation(operationId, clientId, type, amount, newBalance, date, label);

            when(bankClientRepository.existsById(any())).thenReturn(true);
            when(bankOperationRepository.findLatestOperation(any())).thenReturn(Optional.of(lastOperation));
            when(bankOperationRepository.save(any())).thenReturn(expectedOperation);


            final BankOperation operation = bankService.deposit(clientId, amount, label);

            assertEquals(expectedOperation, operation);

            final InOrder orderVerifier = inOrder(bankOperationRepository, bankClientRepository);
            orderVerifier.verify(bankClientRepository).existsById(clientId);
            orderVerifier.verify(bankOperationRepository).findLatestOperation(clientId);
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
    class Withdrawal {
        @Test
        void returnBankOperation() throws ClientNotFoundException, NegativeAmountException, NegativeBalanceException {
            final UUID operationId = UUID.randomUUID();
            final UUID clientId = UUID.randomUUID();
            final LocalDate date = LocalDate.now(clock);
            final Amount amount = Amount.of(BigInteger.ONE);
            final BankOperation.OperationType type = BankOperation.OperationType.WITHDRAWAL;

            final BankOperation lastOperation = new BankOperation(null, clientId, type, Amount.of(BigInteger.TEN), Amount.of(BigInteger.TEN), date, "last");
            final Amount newBalance = lastOperation.balance().subtract(amount);

            final BankOperation operationToSave = new BankOperation(null, clientId, type, amount, newBalance, date, "withdrawal");
            final BankOperation expectedOperation = new BankOperation(operationId, clientId, type, amount, newBalance, date, "withdrawal");

            when(bankClientRepository.existsById(any())).thenReturn(true);
            when(bankOperationRepository.findLatestOperation(any())).thenReturn(Optional.of(lastOperation));
            when(bankOperationRepository.save(any())).thenReturn(expectedOperation);


            final BankOperation operation = bankService.withdraw(clientId, amount);

            assertEquals(expectedOperation, operation);

            final InOrder orderVerifier = inOrder(bankOperationRepository, bankClientRepository);
            orderVerifier.verify(bankClientRepository).existsById(clientId);
            orderVerifier.verify(bankOperationRepository).findLatestOperation(clientId);
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

        @Test
        void throwOnNegativeBalance() throws NegativeAmountException {
            final UUID clientId = UUID.randomUUID();
            final LocalDate date = LocalDate.now(clock);
            final Amount amount = Amount.of(BigInteger.ONE);
            final BankOperation.OperationType type = BankOperation.OperationType.WITHDRAWAL;

            final BankOperation lastOperation = new BankOperation(null, clientId, type, Amount.ZERO, Amount.ZERO, date, "last");

            when(bankClientRepository.existsById(any())).thenReturn(true);
            when(bankOperationRepository.findLatestOperation(any())).thenReturn(Optional.of(lastOperation));


            assertThrows(NegativeBalanceException.class, () -> bankService.withdraw(clientId, amount));

            final InOrder orderVerifier = inOrder(bankOperationRepository, bankClientRepository);
            orderVerifier.verify(bankClientRepository).existsById(clientId);
            orderVerifier.verify(bankOperationRepository).findLatestOperation(clientId);
            orderVerifier.verifyNoMoreInteractions();
        }
    }
}