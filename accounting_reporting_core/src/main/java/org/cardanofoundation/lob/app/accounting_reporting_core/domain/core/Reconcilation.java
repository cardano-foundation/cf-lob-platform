package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.persistence.Enumerated;
import lombok.*;

import static jakarta.persistence.EnumType.STRING;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Reconcilation {

    @Enumerated(STRING)
    private ReconcilationCode source;

    @Enumerated(STRING)
    private ReconcilationCode sink;

}
