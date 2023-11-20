package io.smanicome.bank_account.visual;

import io.smanicome.bank_account.Statement;

import java.util.List;

public interface StatementFormatter {
    List<String> format(Statement statement);
}
