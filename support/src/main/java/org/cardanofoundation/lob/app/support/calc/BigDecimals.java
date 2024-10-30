package org.cardanofoundation.lob.app.support.calc;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.BinaryOperator;

@UtilityClass
public class BigDecimals {

    public BigDecimal sum(BigDecimal... values) {
        return Arrays.stream(values)
                .map(value -> value != null ? value : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal sum(Summable... values) {
        return Summable.sum(values);
    }

    public BigDecimal normalise(BigDecimal value) {
        if (value == null) {
            return null;
        }

        return value.setScale(8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
    }

    public String normaliseEngineeringString(BigDecimal dec) {
        return normalise(dec).toEngineeringString();
    }

}
