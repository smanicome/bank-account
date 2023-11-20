package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;

import java.util.UUID;

public interface IBankService {
    BankOperation deposit(UUID clientId, Amount amount, String label) throws ClientNotFoundException;
}
