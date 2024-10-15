package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Audited
@EntityListeners({ AuditingEntityListener.class })
public class TransactionViolation {

    @NotNull
    @Builder.Default
    private String txItemId = "";

    @NotNull
    @Enumerated(STRING)
    private TransactionViolationCode code;

    @NotNull
    @Enumerated(STRING)
    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity severity;

    @Builder.Default
    private String subCode = "";

    @NotNull
    @Enumerated(STRING)
    private Source source;

    @NotBlank
    private String processorModule;

    @NotNull
    @Builder.Default
    @Type(value = io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @DiffIgnore
    private Map<String, Object> bag = new HashMap<>();

    @Override
    public String toString() {
        return STR."TransactionViolation{code=\{code}, txItemId=\{txItemId}";
    }

    public void setTxItemId(Optional<String> txItemId) {
        this.txItemId = txItemId.orElse("");
    }

    public Optional<String> getTxItemId() {
        return Optional.ofNullable(txItemId).filter(s -> !s.isEmpty());
    }

}
