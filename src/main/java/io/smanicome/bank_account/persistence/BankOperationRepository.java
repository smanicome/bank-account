package io.smanicome.bank_account.persistence;

import io.smanicome.bank_account.BankOperation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankOperationRepository {
    BankOperation save(BankOperation operation);
    Optional<BankOperation> findLatestOperationByClientId(UUID clientId);

    List<BankOperation> findByAccountId(UUID clientId);
}
