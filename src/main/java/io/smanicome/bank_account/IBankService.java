package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.ClientNotFoundException;
import io.smanicome.bank_account.exceptions.NegativeDepositException;

import java.math.BigInteger;
import java.util.UUID;

public interface IBankService {
    BankOperation deposit(UUID id, BigInteger amount, String label) throws ClientNotFoundException, NegativeDepositException;
}
