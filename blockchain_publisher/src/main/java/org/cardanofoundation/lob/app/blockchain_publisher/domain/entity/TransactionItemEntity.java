package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

import static jakarta.persistence.FetchType.EAGER;

@Getter
@Setter
@Entity(name = "blockchain_publisher.TransactionItemEntity")
@Table(name = "blockchain_publisher_transaction_item")
@NoArgsConstructor
@AllArgsConstructor
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionItemEntity extends AuditEntity implements Persistable<String> {

    @Id
    @Column(name = "transaction_item_id", nullable = false)
    private String id;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Column(name = "amount_fcy", nullable = false)
    private BigDecimal amountFcy;

    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "account_event_code", nullable = false)),
            @AttributeOverride(name = "name", column = @Column(name = "account_event_name", nullable = false))
    })
    private AccountEvent accountEvent;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "customerCode", column = @Column(name = "project_customer_code")),
            @AttributeOverride(name = "name", column = @Column(name = "project_name"))
    })
    private Project project;

    @Embedded
    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "customerCode", column = @Column(name = "cost_center_customer_code")),
            @AttributeOverride(name = "name", column = @Column(name = "cost_center_name"))
    })
    private CostCenter costCenter;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "num", column = @Column(name = "document_num", nullable = false)),
            @AttributeOverride(name = "vat.customerCode", column = @Column(name = "document_vat_customer_code")),
            @AttributeOverride(name = "vat.rate", column = @Column(name = "document_vat_rate")),
            @AttributeOverride(name = "currency.id", column = @Column(name = "document_currency_id")),
            @AttributeOverride(name = "currency.customerCode", column = @Column(name = "document_currency_customer_code", nullable = false)),
            @AttributeOverride(name = "counterparty.customerCode", column = @Column(name = "document_counterparty_customer_code")),
            @AttributeOverride(name = "counterparty.type", column = @Column(name = "document_counterparty_type")),
    })
    private Document document;

    public Optional<AccountEvent> getAccountEvent() {
        return Optional.ofNullable(accountEvent);
    }

    public Optional<Project> getProject() {
        return Optional.ofNullable(project);
    }

    public Optional<CostCenter> getCostCenter() {
        return Optional.ofNullable(costCenter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionItemEntity that = (TransactionItemEntity) o;

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

}
