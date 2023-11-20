package io.smanicome.bank_account.visual;

import io.smanicome.bank_account.Amount;
import io.smanicome.bank_account.BankOperation;
import io.smanicome.bank_account.Statement;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabularStatementFormatter implements StatementFormatter {
    private static final String COLUMN_SEPARATOR = " | ";
    private static final String DATE_FORMAT_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final int OPERATION_TYPE_LENGTH = 10;
    private static final int DATE_TIME_LENGTH = DATE_FORMAT_PATTERN.length();
    private static final int AMOUNT_LENGTH = 24;

    private static final int LINE_LENGTH = OPERATION_TYPE_LENGTH + DATE_TIME_LENGTH + AMOUNT_LENGTH + COLUMN_SEPARATOR.length() * 2;

    @Override
    public List<String> format(Statement statement) {

        final var lines = new ArrayList<>(buildHeaders(statement));
        lines.add(addFrameSides(separator()));

        if(!statement.bankOperations().isEmpty()) {
            lines.add(addFrameSides(buildOperationsHeader()));

            lines.add(addFrameSides(separator()));
            lines.addAll(buildFormattedOperationsWithSeparators(statement));
            lines.add(addFrameSides(separator()));
        }

        return lines;
    }

    private String separator() {
        return "-".repeat(LINE_LENGTH);
    }

    private List<String> buildHeaders(Statement statement) {
        final var title = "STATEMENT OF ACCOUNT N°" + statement.accountId();
        final var statementDate = statement.date().format(FORMATTER);
        final var balance = "BALANCE " + getStatementBalance(statement).toCurrencyString();

        return Stream.of(separator(), title, statementDate, balance)
                .map(s -> center(s, LINE_LENGTH))
                .map(this::addFrameSides)
                .toList();
    }

    private Amount getStatementBalance(Statement statement) {
        return statement.bankOperations()
            .stream()
            .max(Comparator.comparing(BankOperation::date))
            .map(BankOperation::balance)
            .orElse(Amount.ZERO);
    }

    private String buildOperationsHeader() {
        final var typeHeader = String.format("%-" + OPERATION_TYPE_LENGTH + "s", "TYPE");

        final var dateHeader = center("DATE", DATE_TIME_LENGTH);

        final var amountHeader = String.format("%" + AMOUNT_LENGTH + "s", "AMOUNT");

        return String.join(COLUMN_SEPARATOR, List.of(typeHeader, dateHeader, amountHeader));
    }

    private List<String> buildFormattedOperationsWithSeparators(Statement statement) {
        return statement.bankOperations().stream()
                .sorted((a, b) -> b.date().compareTo(a.date()))
                .map(this::formatOperation)
                .map(formattedOperation -> formattedOperation.collect(Collectors.joining(COLUMN_SEPARATOR)))
                .map(this::addFrameSides)
                .toList();
    }

    private Stream<String> formatOperation(BankOperation operation) {
        final var operationType = operation.operationType().name();
        final var formattedOperationType = String.format("%-" + OPERATION_TYPE_LENGTH + "s", operationType);

        final var date = operation.date().format(FORMATTER);
        final var formattedDate = center(date, DATE_TIME_LENGTH);

        final var amount = operation.amount().toCurrencyString();
        final var formattedAmount = String.format("%" + AMOUNT_LENGTH + "s", amount);

        return Stream.of(formattedOperationType, formattedDate, formattedAmount);
    }

    private String center(String value, int length) {
        if(value.length() >= length) return value;

        var remainingSpace = length - value.length();
        if(remainingSpace % 2 == 1) {
            remainingSpace ++;
            value += " ";
        }

        final var leftPaddedString = String.format("%" + ((remainingSpace / 2) + value.length()) + "s", value);
        return String.format("%-" + length + "s", leftPaddedString);
    }

    private String addFrameSides(String value) {
        return "| " + value + " |";
    }
}
