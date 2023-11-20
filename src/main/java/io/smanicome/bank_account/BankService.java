package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeAmountException;
import io.smanicome.bank_account.exceptions.NegativeBalanceException;
import io.smanicome.bank_account.persistence.BankClientRepository;
import io.smanicome.bank_account.persistence.BankOperationRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

public class BankService implements IBankService {
    private final BankClientRepository bankClientRepository;
    private final BankOperationRepository bankOperationRepository;
    private final Clock clock;

    public BankService(BankClientRepository bankClientRepository, BankOperationRepository bankOperationRepository, Clock clock) {
        this.bankClientRepository = bankClientRepository;
        this.bankOperationRepository = bankOperationRepository;
        this.clock = clock;
    }

    @Override
    public BankOperation deposit(UUID clientId, Amount amount, String label) throws ClientNotFoundException {
        assertThatClientExists(clientId);

        final Amount currentBalance = getCurrentBalance(clientId);

        try {
            final Amount newBalance = currentBalance.add(amount);
            final BankOperation operation = new BankOperation(
                    null,
                    clientId,
                    BankOperation.OperationType.DEPOSIT,
                    amount,
                    newBalance,
                    LocalDate.now(clock),
                    label);

            return bankOperationRepository.save(operation);
        } catch (NegativeAmountException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public BankOperation withdraw(UUID clientId, Amount amount) throws ClientNotFoundException, NegativeBalanceException {
        assertThatClientExists(clientId);

        final Amount currentBalance = getCurrentBalance(clientId);

        try {
            final Amount newBalance = currentBalance.subtract(amount);
            final BankOperation operation = new BankOperation(
                    null,
                    clientId,
                    BankOperation.OperationType.WITHDRAWAL,
                    amount,
                    newBalance,
                    LocalDate.now(clock),
                    "withdrawal");

            return bankOperationRepository.save(operation);
        } catch (NegativeAmountException e) {
            throw new NegativeBalanceException();
        }
    }

    private void assertThatClientExists(UUID clientId) throws ClientNotFoundException {
        final boolean clientExists = bankClientRepository.existsById(clientId);
        if(!clientExists) {
            throw new ClientNotFoundException();
        }
    }

    private Amount getCurrentBalance(UUID clientId) {
        return bankOperationRepository.findLatestOperation(clientId)
                .map(BankOperation::balance)
                .orElse(Amount.ZERO);
    }
}
