package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Getter
@Setter
@Entity(name = "blockchain_publisher.TransactionEntity")
@Table(name = "blockchain_publisher_transaction")
@NoArgsConstructor
@AllArgsConstructor
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionEntity extends AuditEntity implements Persistable<String> {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private String id;

    @Column(name = "internal_number", nullable = false)
    private String internalNumber;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "name", column = @Column(name = "organisation_name")),
            @AttributeOverride(name = "taxIdNumber", column = @Column(name = "organisation_tax_id_number")),
            @AttributeOverride(name = "countryCode", column = @Column(name = "organisation_country_code")),
            @AttributeOverride(name = "currencyId", column = @Column(name = "organisation_currency_id")),
    })
    private Organisation organisation;

    @Column(name = "type", nullable = false)
    @Enumerated(STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "accounting_period", nullable = false)
    private YearMonth accountingPeriod;

    @Nullable
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "transactionHash", column = @Column(name = "l1_transaction_hash")),
            @AttributeOverride(name = "absoluteSlot", column = @Column(name = "l1_absolute_slot")),
            @AttributeOverride(name = "creationSlot", column = @Column(name = "l1_creation_slot")),
            @AttributeOverride(name = "assuranceLevel", column = @Column(name = "l1_assurance_level")),
            @AttributeOverride(name = "publishStatus", column = @Column(name = "l1_publish_status"))
    })
    private L1SubmissionData l1SubmissionData;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true, fetch = EAGER)
    private Set<TransactionItemEntity> items = new LinkedHashSet<>();

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof TransactionEntity te) {
            return id.equals(te.getId());
        }

        return false;
    }

    public Optional<L1SubmissionData> getL1SubmissionData() {
        return Optional.ofNullable(l1SubmissionData);
    }

    @Override
    public String getId() {
        return id;
    }

}