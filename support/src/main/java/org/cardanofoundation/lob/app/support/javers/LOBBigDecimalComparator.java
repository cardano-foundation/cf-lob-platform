package org.cardanofoundation.lob.app.support.javers;

import org.cardanofoundation.lob.app.support.calc.BigDecimals;
import org.javers.core.diff.custom.CustomValueComparator;

import java.math.BigDecimal;

public class LOBBigDecimalComparator implements CustomValueComparator<BigDecimal> {

    @Override
    public boolean equals(BigDecimal a, BigDecimal b) {
        return round(a).equals(round(b));
    }

    @Override
    public String toString(BigDecimal value) {
        return round(value).toString();
    }

    private BigDecimal round(BigDecimal val) {
        return BigDecimals.normalise(val);
    }

}
