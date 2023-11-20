package io.smanicome.bank_account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Statement(UUID accountId, List<BankOperation> bankOperations, LocalDateTime date) {
}
