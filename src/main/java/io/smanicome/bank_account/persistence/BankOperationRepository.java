package io.smanicome.bank_account.persistence;

import io.smanicome.bank_account.BankOperation;

import java.util.Optional;
import java.util.UUID;

public interface BankOperationRepository {
    BankOperation save(BankOperation operation);
    Optional<BankOperation> findLatestOperation(UUID id);
}
