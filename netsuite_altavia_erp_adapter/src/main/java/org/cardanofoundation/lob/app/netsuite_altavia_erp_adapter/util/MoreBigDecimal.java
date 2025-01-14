package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util;

import io.micrometer.common.lang.Nullable;

import java.math.BigDecimal;

public class MoreBigDecimal {

    public static BigDecimal zeroForNull(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value;
    }

    public static BigDecimal substract(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    public static BigDecimal substractNullFriendly(@Nullable BigDecimal a,
                                                   @Nullable BigDecimal b) {
        return zeroForNull(a).subtract(zeroForNull(b));
    }

}
