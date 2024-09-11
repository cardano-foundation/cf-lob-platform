package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
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
