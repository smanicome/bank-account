package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeBalanceException;

import java.util.UUID;

public interface IBankService {
    BankOperation deposit(UUID clientId, Amount amount, String label) throws ClientNotFoundException;
    BankOperation withdraw(UUID clientId, Amount amount) throws ClientNotFoundException, NegativeBalanceException;
}