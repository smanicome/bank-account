package io.smanicome.bank_account.persistence;

import java.util.UUID;

public interface BankClientRepository {
    boolean existsById(UUID id);
}
