package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class Counterparty {

    private String customerCode;

    @Enumerated(STRING)
    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type type;

}
