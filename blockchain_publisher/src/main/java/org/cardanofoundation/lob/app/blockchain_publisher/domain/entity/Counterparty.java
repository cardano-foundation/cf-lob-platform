package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

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
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type type;

}
