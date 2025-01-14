package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import java.util.Optional;

import javax.annotation.Nullable;

import lombok.*;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class BalanceSheetData  {

    @Nullable
    private Assets assets;

    @Nullable
    private Liabilities liabilities;

    @Nullable
    private Capital capital;

    public Optional<Assets> getAssets() {
        return Optional.ofNullable(assets);
    }

    public Optional<Liabilities> getLiabilities() {
        return Optional.ofNullable(liabilities);
    }

    public Optional<Capital> getCapital() {
        return Optional.ofNullable(capital);
    }

}
