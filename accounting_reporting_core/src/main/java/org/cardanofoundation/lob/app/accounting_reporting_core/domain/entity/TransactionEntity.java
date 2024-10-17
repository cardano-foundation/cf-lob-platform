package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOBVersionSourceRelevant;
import org.cardanofoundation.lob.app.support.spring_audit.CommonEntity;
import org.hibernate.envers.Audited;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.OK;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Entity(name = "accounting_reporting_core.TransactionEntity")
@Table(name = "accounting_core_transaction")
@NoArgsConstructor
@AllArgsConstructor
@Audited
@EntityListeners({ OverallStatusTransactionEntityListener.class, AuditingEntityListener.class })
public class TransactionEntity extends CommonEntity implements Persistable<String> {

    @Id
    @Column(name = "transaction_id", nullable = false)
    @LOBVersionSourceRelevant
    @Setter
    private String id;

    @Column(name = "transaction_internal_number", nullable = false)
    @LOBVersionSourceRelevant
    @Getter
    @Setter
    private String transactionInternalNumber;

    @Column(name = "batch_id", nullable = false)
    @DiffIgnore
    @Getter
    @Setter
    private String batchId;

    @Column(name = "accounting_period", nullable = false)
    @Getter
    @Setter
    private YearMonth accountingPeriod;

    @Column(name = "type", nullable = false)
    @Enumerated(STRING)
    @LOBVersionSourceRelevant
    @Getter
    @Setter
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    @LOBVersionSourceRelevant
    @Getter
    @Setter
    private LocalDate entryDate;

    @Column(name = "ledger_dispatch_status", nullable = false)
    @Enumerated(STRING)
    @Getter
    @Setter
    private LedgerDispatchStatus ledgerDispatchStatus = NOT_DISPATCHED;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "name", column = @Column(name = "organisation_name")),
            @AttributeOverride(name = "countryCode", column = @Column(name = "organisation_country_code")),
            @AttributeOverride(name = "taxIdNumber", column = @Column(name = "organisation_tax_id_number")),
            @AttributeOverride(name = "currencyId", column = @Column(name = "organisation_currency_id"))
    })
    @Getter
    @Setter
    private Organisation organisation;

    @Column(name = "automated_validation_status", nullable = false)
    @Enumerated(STRING)
    @Getter
    @Setter
    private ValidationStatus automatedValidationStatus = ValidationStatus.VALIDATED;

    @Column(name = "transaction_approved", nullable = false)
    @Getter
    @Setter
    private Boolean transactionApproved = false;

    @Column(name = "ledger_dispatch_approved", nullable = false)
    @Getter
    @Setter
    private Boolean ledgerDispatchApproved = false;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true, fetch = EAGER)
    @Getter
    @Setter
    private Set<TransactionItemEntity> items = new LinkedHashSet<>();

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reconcilation_id")
    @Nullable
    private ReconcilationEntity lastReconcilation;

    @Column(name = "user_comment")
    @Nullable
    private String userComment;

    @Column(name = "overall_status", nullable = false)
    @Enumerated(STRING)
    @DiffIgnore
    @Getter
    @Setter
    private TransactionStatus overallStatus;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "source", column = @Column(name = "reconcilation_source")),
        @AttributeOverride(name = "sink", column = @Column(name = "reconcilation_sink")),
        @AttributeOverride(name = "finalStatus", column = @Column(name = "reconcilation_final_status"))
    })
    @Nullable
    @DiffIgnore
    private Reconcilation reconcilation;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "accounting_core_transaction_violation", joinColumns = @JoinColumn(name = "transaction_id"))
    @Audited
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "code", nullable = false)),
            @AttributeOverride(name = "subCode", column = @Column(name = "sub_code", nullable = false)),
            @AttributeOverride(name = "type", column = @Column(name = "type", nullable = false)),
            @AttributeOverride(name = "txItemId", column = @Column(name = "tx_item_id", nullable = false)),
            @AttributeOverride(name = "source", column = @Column(name = "source", nullable = false)),
            @AttributeOverride(name = "processorModule", column = @Column(name = "processor_module", nullable = false)),
            @AttributeOverride(name = "bag", column = @Column(name = "bag", nullable = false))
    })
    @DiffIgnore
    @Getter
    @Setter
    private Set<TransactionViolation> violations = new LinkedHashSet<>();

    public boolean allApprovalsPassedForTransactionDispatch() {
        return transactionApproved && ledgerDispatchApproved;
    }

    public void addViolation(TransactionViolation transactionViolation) {
        this.violations.add(transactionViolation);

        recalcValidationStatus();
    }

    public void clearAllViolations() {
        violations.clear();
        recalcValidationStatus();
    }

    public void clearAllItemsRejectionsSource(Source source) {
        for (TransactionItemEntity txItem : items) {
            if (txItem.getRejection().stream().anyMatch(rejection -> rejection.getRejectionReason().getSource().equals(source))) {
                txItem.setRejection(Optional.empty());
            }

        }
    }

    public boolean hasAnyRejection() {
        return items.stream().anyMatch(item -> item.getRejection().isPresent());
    }

    public boolean hasAnyRejection(Source source) {
        for (TransactionItemEntity txItem : items) {
            if (txItem.getRejection().stream().anyMatch(rejection -> rejection.getRejectionReason().getSource().equals(source))) {
                return true;
            }

        }
        return false;
    }

    public boolean isRejectionFree() {
        return !hasAnyRejection();
    }

    private void recalcValidationStatus() {
        automatedValidationStatus = violations.isEmpty() ? ValidationStatus.VALIDATED : FAILED;
    }

    public Optional<TransactionItemEntity> findItemById(String txItemId) {
        return items.stream().filter(item -> txItemId.equals(item.getId())).findFirst();
    }

    public boolean isDispatchable() {
        return overallStatus == OK
                && ledgerDispatchStatus == NOT_DISPATCHED;
    }

    public Optional<Reconcilation> getReconcilation() {
        return Optional.ofNullable(reconcilation);
    }

    public void setReconcilation(Optional<Reconcilation> reconcilation) {
        this.reconcilation = reconcilation.orElse(null);
    }

    public Optional<ReconcilationEntity> getLastReconcilation() {
        return Optional.ofNullable(lastReconcilation);
    }

    public void setLastReconcilation(Optional<ReconcilationEntity> parentReconcilation) {
        this.lastReconcilation = parentReconcilation.orElse(null);
    }

    public Optional<String> getUserComment() {
        return Optional.ofNullable(userComment);
    }

    public void setUserComment(Optional<String> userComment) {
        this.userComment = userComment.orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        val that = (TransactionEntity) o;

        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return STR."TransactionEntity{id='\{id}\{'\''}, transactionInternalNumber='\{transactionInternalNumber}\{'\''}, batchId='\{batchId}'}";
    }

}
