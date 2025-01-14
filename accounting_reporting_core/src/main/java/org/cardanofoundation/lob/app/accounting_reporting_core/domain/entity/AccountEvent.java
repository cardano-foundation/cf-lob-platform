package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.envers.Audited;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
@Audited
@ToString
public class AccountEvent {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

}
