package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import static jakarta.persistence.EnumType.STRING;

import jakarta.persistence.Enumerated;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Rejection {

    @Enumerated(STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private RejectionReason rejectionReason;

}
