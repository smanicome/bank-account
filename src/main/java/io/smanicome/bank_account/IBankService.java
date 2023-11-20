package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeAmountException;
import io.smanicome.bank_account.exceptions.NegativeBalanceException;

import java.math.BigInteger;
import java.util.UUID;

public interface IBankService {
    BankOperation deposit(UUID clientId, Amount amount, String label) throws ClientNotFoundException, NegativeAmountException;
}
