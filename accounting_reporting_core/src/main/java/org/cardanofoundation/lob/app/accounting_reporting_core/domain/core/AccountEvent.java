package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class AccountEvent {

    private @Size(min = 1, max =  255) String code;

    private @Size(min = 1, max =  255) String name;

}
