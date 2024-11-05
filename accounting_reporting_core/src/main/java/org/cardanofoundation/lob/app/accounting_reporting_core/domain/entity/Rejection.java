package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import static jakarta.persistence.EnumType.STRING;

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
