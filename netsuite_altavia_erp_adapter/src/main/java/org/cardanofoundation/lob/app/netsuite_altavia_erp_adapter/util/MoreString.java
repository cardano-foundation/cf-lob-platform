package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public final class MoreString {

    public static Optional<String> normaliseString(@Nullable String s) {
        if (s == null) {
            return Optional.empty();
        }
        if (StringUtils.isBlank(s.trim())) {
            return Optional.empty();
        }

        return Optional.of(s);
    }

}
