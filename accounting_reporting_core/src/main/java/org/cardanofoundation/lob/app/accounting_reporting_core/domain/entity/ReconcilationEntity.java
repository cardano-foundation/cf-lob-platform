package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReconcilationStatus;
import org.cardanofoundation.lob.app.support.spring_audit.AuditEntity;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Entity(name = "accounting_reporting_core.ReconcilationEntity")
@Table(name = "accounting_core_reconcilation")
@NoArgsConstructor
public class ReconcilationEntity extends AuditEntity implements Persistable<String> {

    @Override
    public String getId() {
        return id;
    }

    @Id
    @Column(name = "reconcilation_id", nullable = false)
    @NotNull
    @Setter
    private String id;

    @Column(name = "organisation_id", nullable = false)
    @Getter
    @Setter
    private String organisationId;

    @Column(name = "from_date")
    @Nullable
    private LocalDate from;

    @Column(name = "to_date")
    @Nullable
    private LocalDate to;

    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "detail_code")),
            @AttributeOverride(name = "subCode", column = @Column(name = "detail_subcode")),
            @AttributeOverride(name = "bag", column = @Column(name = "detail_bag"))
    })
    private Details details;

    @Column(name = "processed_tx_count")
    @Getter
    @Setter
    private long processedTxCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Getter
    @Setter
    private ReconcilationStatus status = ReconcilationStatus.STARTED;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "accounting_core_reconcilation_violation", joinColumns = @JoinColumn(name = "reconcilation_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "transactionId", column = @Column(name = "transaction_id", nullable = false)),
            @AttributeOverride(name = "rejectionCode", column = @Column(name = "rejection_code", nullable = false)),
            @AttributeOverride(name = "transactionInternalNumber", column = @Column(name = "transaction_internal_number", nullable = false)),
            @AttributeOverride(name = "transactionEntryDate", column = @Column(name = "transaction_entry_date", nullable = false)),
            @AttributeOverride(name = "transactionType", column = @Column(name = "transaction_type", nullable = false)),
            @AttributeOverride(name = "amountLcy", column = @Column(name = "amount_lcy", nullable = false))
    })
    @Setter
    @Getter
    private Set<ReconcilationViolation> violations = new LinkedHashSet<>();

    public void incrementMissingTxsCount(int delta) {
        processedTxCount += delta;
    }

    public void addViolation(ReconcilationViolation violation) {
        violations.add(violation);
    }

    public void removeViolation(ReconcilationViolation violation) {
        violations.remove(violation);
    }

    public Optional<Details> getDetails() {
        return Optional.ofNullable(details);
    }

    public void setDetails(Optional<Details> details) {
        this.details = details.orElse(null);
    }

    public Optional<LocalDate> getFrom() {
        return Optional.ofNullable(from);
    }

    public Optional<LocalDate> getTo() {
        return Optional.ofNullable(to);
    }

    public void setFrom(Optional<LocalDate> localDate) {
        this.from = localDate.orElse(null);
    }

    public void setTo(Optional<LocalDate> localDate) {
        this.to = localDate.orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        val that = (ReconcilationEntity) o;

        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return STR."ReconcilationEntity{id='\{id}\{'\''}, createdBy='\{createdBy}\{'\''}, updatedBy='\{updatedBy}\{'\''}, createdAt=\{createdAt}, updatedAt=\{updatedAt}\{'}'}";
    }

}
