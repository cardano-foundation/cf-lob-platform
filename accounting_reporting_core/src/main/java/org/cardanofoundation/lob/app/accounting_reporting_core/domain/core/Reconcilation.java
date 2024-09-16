package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.persistence.Enumerated;
import lombok.*;

import javax.annotation.Nullable;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Reconcilation {

    @Enumerated(STRING)
    @Nullable
    private ReconcilationCode source;

    @Enumerated(STRING)
    @Nullable
    private ReconcilationCode sink;

    @Enumerated(STRING)
    @Nullable
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
