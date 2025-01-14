package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import lombok.*;

import javax.annotation.Nullable;
import java.util.Optional;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Liabilities {

    @Nullable
    private NonCurrentLiabilities nonCurrentLiabilities;

    @Nullable
    private CurrentLiabilities currentLiabilities;

    public Optional<NonCurrentLiabilities> getNonCurrentLiabilities() {
        return Optional.ofNullable(nonCurrentLiabilities);
    }

    public Optional<CurrentLiabilities> getCurrentLiabilities() {
        return Optional.ofNullable(currentLiabilities);
    }

}
