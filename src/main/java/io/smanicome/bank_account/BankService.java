package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeAmountException;
import io.smanicome.bank_account.exceptions.NegativeBalanceException;
import io.smanicome.bank_account.persistence.BankClientRepository;
import io.smanicome.bank_account.persistence.BankOperationRepository;

import java.math.BigInteger;
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
    public BankOperation deposit(UUID clientId, Amount amount, String label) throws ClientNotFoundException, NegativeAmountException {
        final boolean clientExists = bankClientRepository.existsById(clientId);
        if(!clientExists) {
            throw new ClientNotFoundException();
        }

        final Amount currentBalance = bankOperationRepository.findLatestOperation(clientId)
                .map(BankOperation::balance)
                .orElse(Amount.ZERO);

        final Amount newBalance = currentBalance.add(amount);

        final BankOperation operation = new BankOperation(null, clientId, amount, newBalance, LocalDate.now(clock), label);

        return bankOperationRepository.save(operation);
    }
}
