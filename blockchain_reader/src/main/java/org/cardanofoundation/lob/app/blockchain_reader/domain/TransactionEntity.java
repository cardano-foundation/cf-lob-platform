package org.cardanofoundation.lob.app.blockchain_reader.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.support.spring_audit.AuditEntity;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@Entity(name = "blockchain_reader.TransactionEntity")
@Table(name = "blockchain_reader_transaction")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity extends AuditEntity implements Persistable<String> {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "absolute_slot", nullable = false)
    private Long absoluteSlot;

    @Column(name = "tx_hash", nullable = false)
    private String txHash;

    @Override
    public String getId() {
        return id;
    }

}
