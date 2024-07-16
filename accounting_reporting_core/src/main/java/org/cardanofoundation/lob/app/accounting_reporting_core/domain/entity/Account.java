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
public class Account {

    @NotBlank
    @LOB_ERPSourceVersionRelevant
    private String code;

    @Nullable
    private String name;

    @Nullable
    private String refCode;

    public Optional<String> getRefCode() {
        return Optional.ofNullable(refCode);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

}
