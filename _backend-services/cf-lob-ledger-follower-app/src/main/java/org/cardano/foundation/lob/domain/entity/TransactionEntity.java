package org.cardano.foundation.lob.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@Entity(name = "TransactionEntity")
@Table(name = "blockchain_reader_transaction")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity extends AuditEntity implements Persistable<String> {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "l1_absolute_slot", nullable = false)
    private Long l1AbsoluteSlot;

    @Column(name = "l1_transaction_hash", nullable = false)
    private String l1TransactionHash;

    @Override
    public String getId() {
        return id;
    }

}
