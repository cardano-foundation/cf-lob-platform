package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class Organisation {

    @NotBlank
    @LOBVersionSourceRelevant
    private String id;

    @Nullable
    private String name;

    @Nullable
    private String countryCode;

    @Nullable
    private String taxIdNumber;

    @Nullable
    private String currencyId;

    @Nullable
    @Transient
    private String adminEmail;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getCountryCode() {
        return Optional.ofNullable(countryCode);
    }

    public Optional<String> getTaxIdNumber() {
        return Optional.ofNullable(taxIdNumber);
    }

    public Optional<String> getAdminEmail() {
        return Optional.ofNullable(adminEmail);
    }

}
