package org.cardanofoundation.lob.app.support.calc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.BinaryOperator;

@FunctionalInterface
public interface Summable extends BinaryOperator<BigDecimal> {

    BigDecimal sumOf();

    default BigDecimal apply(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    static BigDecimal sum(Summable... values) {
        return Arrays.stream(values)
                .map(value -> value != null ? value.sumOf() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
