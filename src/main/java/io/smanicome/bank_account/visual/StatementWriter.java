package io.smanicome.bank_account.visual;

import io.smanicome.bank_account.Statement;

public interface StatementWriter {
    void write(Statement statement);
}
