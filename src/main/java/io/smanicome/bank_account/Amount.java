package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.NegativeAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Amount implements Comparable<Amount> {
    public static final Amount ZERO = new Amount(BigDecimal.ZERO);
    private final BigDecimal value;

    private Amount(BigDecimal value) {
        this.value = value;
    }

    public static Amount of(BigDecimal value) throws NegativeAmountException {
        if(value.signum() == -1) throw new NegativeAmountException();
        return new Amount(value);
    }


    public Amount add(Amount amount) throws NegativeAmountException {
        return Amount.of(value.add(amount.value));
    }

    public Amount subtract(Amount amount) throws NegativeAmountException {
        return Amount.of(value.subtract(amount.value));
    }

    @Override
    public int compareTo(Amount amount) {
        return value.compareTo(amount.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Amount amount = (Amount) o;
        return Objects.equals(value, amount.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String toCurrencyString() {
        return value.setScale(2, RoundingMode.HALF_EVEN).toString();
    }
}
