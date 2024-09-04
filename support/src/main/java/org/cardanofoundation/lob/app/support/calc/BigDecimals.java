package org.cardanofoundation.lob.app.support.calc;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class BigDecimals {

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
