package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report;

import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.Nullable;

import lombok.*;

@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class CostOfServicesAndGoods {

    @Nullable
    private BigDecimal costOfProvidingServices;

    public Optional<BigDecimal> getCostOfProvidingServices() {
        return Optional.ofNullable(costOfProvidingServices);
    }

}
