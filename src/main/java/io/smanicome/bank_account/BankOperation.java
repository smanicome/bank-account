package io.smanicome.bank_account;

import java.time.LocalDate;
import java.util.UUID;

public record BankOperation(UUID id, UUID clientId, OperationType operationType, Amount amount, Amount balance, LocalDate date, String label) {
    public enum OperationType {
        DEPOSIT, WITHDRAWAL
    }
}
