package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import java.util.Optional;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

import javax.annotation.Nullable;

import org.hibernate.envers.Audited;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@Audited
public class Account {

    @NotBlank
    @LOBVersionSourceRelevant
    @Getter
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

    public void setRefCode(Optional<String> refCode) {
        this.refCode = refCode.orElse(null);
    }

    public void setName(Optional<String> name) {
        this.name = name.orElse(null);
    }

}
