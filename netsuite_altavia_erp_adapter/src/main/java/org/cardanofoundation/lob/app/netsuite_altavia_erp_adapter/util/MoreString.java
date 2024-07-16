package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Optional;

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
