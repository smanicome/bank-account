package io.smanicome.bank_account;

import io.smanicome.bank_account.exceptions.NegativeAmountException;

import java.math.BigInteger;
import java.util.Objects;

public class Amount implements Comparable<Amount> {
    public static final Amount ZERO = new Amount(BigInteger.ZERO);
    private final BigInteger value;

    private Amount(BigInteger value) {
        this.value = value;
    }

    public static Amount of(BigInteger value) throws NegativeAmountException {
        if(value.signum() == -1) throw new NegativeAmountException();
        return new Amount(value);
    }


    public Amount add(Amount amount) throws NegativeAmountException {
        return Amount.of(value.add(amount.value));
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
}
