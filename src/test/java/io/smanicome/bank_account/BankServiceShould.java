package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeAmountException;
import io.smanicome.bank_account.persistence.BankClientRepository;
import io.smanicome.bank_account.persistence.BankOperationRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
    void returnBankOperationOnDeposit() throws ClientNotFoundException, NegativeAmountException {
        final UUID operationId = UUID.randomUUID();
        final UUID clientId = UUID.randomUUID();
        final LocalDate date = LocalDate.now(clock);
        final Amount amount = Amount.of(BigInteger.ONE);
        final String label = "test";

        final BankOperation lastOperation = new BankOperation(null, clientId, Amount.of(BigInteger.TEN), Amount.of(BigInteger.TEN), date, "last");
        final Amount newBalance = lastOperation.balance().add(amount);

        final BankOperation operationToSave = new BankOperation(null, clientId, amount, newBalance, date, label);
        final BankOperation expectedOperation = new BankOperation(operationId, clientId, amount, newBalance, date, label);

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