package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util;

import java.math.BigDecimal;

import io.micrometer.common.lang.Nullable;

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
