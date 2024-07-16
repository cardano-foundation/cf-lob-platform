package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPSourceVersionRelevant;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class CostCenter {

    @NotBlank
    @LOB_ERPSourceVersionRelevant
    private String customerCode;

    @Nullable
    private String externalCustomerCode;

    @Nullable
    private String name;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getExternalCustomerCode() {
        return Optional.ofNullable(externalCustomerCode);
    }

}
