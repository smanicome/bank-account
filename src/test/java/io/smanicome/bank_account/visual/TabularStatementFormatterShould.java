package io.smanicome.bank_account.visual;

import io.smanicome.bank_account.Amount;
import io.smanicome.bank_account.BankOperation;
import io.smanicome.bank_account.Statement;
import io.smanicome.bank_account.exceptions.NegativeAmountException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TabularStatementFormatterShould {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Test
    void formatStatementWithNoOperation() {
        final var formatter = new TabularStatementFormatter();
        final var date = LocalDateTime.now();
        final var accountId = UUID.randomUUID();
        final var operations = List.<BankOperation>of();
        final var statement = new Statement(
                accountId,
                operations,
                date
        );

        final var formattedDate = date.format(FORMATTER);
        final var expectedLines = List.of(
                "| ----------------------------------------------------------- |",
                "| STATEMENT OF ACCOUNT N°" + accountId + " |",
                "|                     " + formattedDate + "                     |",
                "|                         BALANCE 0.00                        |",
                "| ----------------------------------------------------------- |"
        );

        final var lines = formatter.format(statement);


        assertEquals(expectedLines, lines);
    }

    @Test
    void formatStatementWithOneOperation() throws NegativeAmountException, NegativeAmountException {
        final var formatter = new TabularStatementFormatter();

        final var accountId = UUID.randomUUID();
        final var operationDate = LocalDateTime.now();
        final var statementDate = operationDate.plusMinutes(10);

        final var formattedOperationDate = operationDate.format(FORMATTER);
        final var formattedStatementDate = statementDate.format(FORMATTER);

        final var operations = List.of(
                new BankOperation(
                        UUID.randomUUID(),
                        accountId,
                        BankOperation.OperationType.DEPOSIT,
                        Amount.of(BigDecimal.valueOf(2000)),
                        Amount.of(BigDecimal.valueOf(2000)),
                        operationDate,
                        "label"
                )
        );

        final var statement = new Statement(
                accountId,
                operations,
                statementDate
        );

        final var expectedLines = List.of(
                "| ----------------------------------------------------------- |",
                "| STATEMENT OF ACCOUNT N°" + accountId + " |",
                "|                     " + formattedStatementDate + "                     |",
                "|                       BALANCE 2000.00                       |",
                "| ----------------------------------------------------------- |",
                "| TYPE       |         DATE        |                   AMOUNT |",
                "| ----------------------------------------------------------- |",
                "| DEPOSIT    | " + formattedOperationDate + " |                  2000.00 |",
                "| ----------------------------------------------------------- |"
        );

        final var lines = formatter.format(statement);

        assertEquals(expectedLines, lines);
    }

    @Test
    void formatStatementWithMultipleOperations() throws NegativeAmountException {
        final var formatter = new TabularStatementFormatter();

        final var accountId = UUID.randomUUID();
        final var operationDate1 = LocalDateTime.now();
        final var operationDate2 = operationDate1.plusMinutes(10);
        final var operationDate3 = operationDate2.plusMinutes(10);
        final var statementDate = operationDate3.plusMinutes(10);

        final var formattedOperationDate1 = operationDate1.format(FORMATTER);
        final var formattedOperationDate2 = operationDate2.format(FORMATTER);
        final var formattedOperationDate3 = operationDate3.format(FORMATTER);
        final var formattedStatementDate = statementDate.format(FORMATTER);

        final var operations = List.of(
                new BankOperation(
                        UUID.randomUUID(),
                        accountId,
                        BankOperation.OperationType.DEPOSIT,
                        Amount.of(BigDecimal.valueOf(2000)),
                        Amount.of(BigDecimal.valueOf(2000)),
                        operationDate1,
                        "deposit 1"
                ),
                new BankOperation(
                        UUID.randomUUID(),
                        accountId,
                        BankOperation.OperationType.DEPOSIT,
                        Amount.of(BigDecimal.valueOf(5000)),
                        Amount.of(BigDecimal.valueOf(7000)),
                        operationDate2,
                        "deposit 2"
                ),
                new BankOperation(
                        UUID.randomUUID(),
                        accountId,
                        BankOperation.OperationType.WITHDRAWAL,
                        Amount.of(BigDecimal.valueOf(1000)),
                        Amount.of(BigDecimal.valueOf(6000)),
                        operationDate3,
                        "withdrawal"
                )
        );

        final var statement = new Statement(
                accountId,
                operations,
                statementDate
        );

        final var expectedLines = List.of(
                "| ----------------------------------------------------------- |",
                "| STATEMENT OF ACCOUNT N°" + accountId + " |",
                "|                     " + formattedStatementDate + "                     |",
                "|                       BALANCE 6000.00                       |",
                "| ----------------------------------------------------------- |",
                "| TYPE       |         DATE        |                   AMOUNT |",
                "| ----------------------------------------------------------- |",
                "| WITHDRAWAL | " + formattedOperationDate3 + " |                  1000.00 |",
                "| DEPOSIT    | " + formattedOperationDate2 + " |                  5000.00 |",
                "| DEPOSIT    | " + formattedOperationDate1 + " |                  2000.00 |",
                "| ----------------------------------------------------------- |"
        );

        final var lines = formatter.format(statement);

        assertEquals(expectedLines, lines);
    }
}