package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.reconcilation;

import static jakarta.persistence.EnumType.STRING;

import java.util.Optional;

import jakarta.persistence.Enumerated;

import javax.annotation.Nullable;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Reconcilation {

    @Enumerated(STRING)
    @Nullable
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ReconcilationCode source;

    @Enumerated(STRING)
    @Nullable
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ReconcilationCode sink;

    @Enumerated(STRING)
    @Nullable
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ReconcilationCode finalStatus;

    public Optional<ReconcilationCode> getSource() {
        return Optional.ofNullable(source);
    }

    public void setSource(Optional<ReconcilationCode> source) {
        this.source = source.orElse(null);
    }

    public Optional<ReconcilationCode> getSink() {
        return Optional.ofNullable(sink);
    }

    public void setSink(Optional<ReconcilationCode> sink) {
        this.sink = sink.orElse(null);
    }

    public Optional<ReconcilationCode> getFinalStatus() {
        return Optional.ofNullable(finalStatus);
    }

    public void setFinalStatus(Optional<ReconcilationCode> finalStatus) {
        this.finalStatus = finalStatus.orElse(null);
    }

}
