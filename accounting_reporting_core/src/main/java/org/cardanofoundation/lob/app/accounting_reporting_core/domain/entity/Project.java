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
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class Project {

    @NotBlank
    @LOB_ERPSourceVersionRelevant
    private String customerCode;

    @Nullable
    private String name;

    @Nullable
    private String externalCustomerCode;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getExternalCustomerCode() {
        return Optional.ofNullable(externalCustomerCode);
    }

}
