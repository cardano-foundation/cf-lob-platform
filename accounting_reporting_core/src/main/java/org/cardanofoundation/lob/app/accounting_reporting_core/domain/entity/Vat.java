package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import java.math.BigDecimal;
import java.util.Optional;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

import javax.annotation.Nullable;

import org.hibernate.envers.Audited;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@Audited
public class Vat {

    @NotBlank
    @LOBVersionSourceRelevant
    private String customerCode;

    @Nullable
    private BigDecimal rate;

    public Optional<BigDecimal> getRate() {
        return Optional.ofNullable(rate);
    }

}
