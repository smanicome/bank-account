package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeDepositException;
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
    public BankOperation deposit(UUID clientId, BigInteger amount, String label) throws ClientNotFoundException, NegativeDepositException {
        final boolean clientExists = bankClientRepository.existsById(clientId);
        if(!clientExists) {
            throw new ClientNotFoundException();
        }

        if(amount.signum() == -1) {
            throw new NegativeDepositException();
        }

        final BigInteger currentBalance = bankOperationRepository.findLatestOperation(clientId)
                .map(BankOperation::balance)
                .orElse(BigInteger.ZERO);

        final BigInteger newBalance = currentBalance.add(amount);

        final BankOperation operation = new BankOperation(null, clientId, amount, newBalance, LocalDate.now(clock), label);

        return bankOperationRepository.save(operation);
    }
}
