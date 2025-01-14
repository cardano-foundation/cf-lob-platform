package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import lombok.*;

import javax.annotation.Nullable;
import java.util.Optional;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Assets  {

    @Nullable
    private NonCurrentAssets nonCurrentAssets;

    @Nullable
    private CurrentAssets currentAssets;

    public Optional<NonCurrentAssets> getNonCurrentAssets() {
        return Optional.ofNullable(nonCurrentAssets);
    }

    public Optional<CurrentAssets> getCurrentAssets() {
        return Optional.ofNullable(currentAssets);
    }

}
