package io.smanicome.bank_account.visual;

import io.smanicome.bank_account.Statement;

import java.io.PrintStream;
import java.util.List;

public class FormatPrintStatementWriter implements StatementWriter {
    private final StatementFormatter statementFormatter;
    private final PrintStream printStream;

    public FormatPrintStatementWriter(StatementFormatter statementFormatter, PrintStream printStream) {
        this.statementFormatter = statementFormatter;
        this.printStream = printStream;
    }

    @Override
    public void write(Statement statement) {
        final List<String> lines = statementFormatter.format(statement);
        lines.forEach(printStream::println);
    }
}
