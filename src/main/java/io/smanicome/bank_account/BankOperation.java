package io.smanicome.bank_account;

import java.time.LocalDateTime;
import java.util.UUID;

public record BankOperation(UUID id, UUID clientId, OperationType operationType, Amount amount, Amount balance, LocalDateTime date, String label) {
    public enum OperationType {
        DEPOSIT, WITHDRAWAL
    }
}
