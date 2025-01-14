package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import static jakarta.persistence.EnumType.STRING;

import java.util.Optional;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;

import javax.annotation.Nullable;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.envers.Audited;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@Audited
public class Counterparty {

    @LOBVersionSourceRelevant
    private String customerCode;

    @Enumerated(STRING)
    @LOBVersionSourceRelevant
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type type;

    @Nullable
    private String name;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

}
