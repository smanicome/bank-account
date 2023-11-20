package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeAmountException;
import io.smanicome.bank_account.exceptions.NegativeBalanceException;
import io.smanicome.bank_account.persistence.BankClientRepository;
import io.smanicome.bank_account.persistence.BankOperationRepository;
import io.smanicome.bank_account.visual.StatementWriter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BankService implements IBankService {
    private final Clock clock;
    private final BankClientRepository bankClientRepository;
    private final BankOperationRepository bankOperationRepository;
    private final StatementWriter statementWriter;

    public BankService(Clock clock, BankClientRepository bankClientRepository, BankOperationRepository bankOperationRepository, StatementWriter statementWriter) {
        this.clock = clock;
        this.bankClientRepository = bankClientRepository;
        this.bankOperationRepository = bankOperationRepository;
        this.statementWriter = statementWriter;
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
                    LocalDateTime.now(clock),
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
                    LocalDateTime.now(clock),
                    "withdrawal");

            return bankOperationRepository.save(operation);
        } catch (NegativeAmountException e) {
            throw new NegativeBalanceException();
        }
    }

    @Override
    public void printAccountStatement(UUID clientId) throws ClientNotFoundException {
        assertThatClientExists(clientId);

        final List<BankOperation> operations = bankOperationRepository.findByAccountId(clientId);
        final Statement statement = new Statement(clientId, operations, LocalDateTime.now(clock));

        statementWriter.write(statement);
    }

    private void assertThatClientExists(UUID clientId) throws ClientNotFoundException {
        final boolean clientExists = bankClientRepository.existsById(clientId);
        if(!clientExists) {
            throw new ClientNotFoundException();
        }
    }

    private Amount getCurrentBalance(UUID clientId) {
        return bankOperationRepository.findLatestOperationByClientId(clientId)
                .map(BankOperation::balance)
                .orElse(Amount.ZERO);
    }
}
