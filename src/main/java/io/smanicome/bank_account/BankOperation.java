package io.smanicome.bank_account;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

public record BankOperation(UUID id, UUID clientId, Amount amount, Amount balance, LocalDate date, String label) {
}
