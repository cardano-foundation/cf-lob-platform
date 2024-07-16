package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class AccountEvent {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

}
