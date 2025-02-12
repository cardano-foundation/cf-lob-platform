package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;

import javax.annotation.Nullable;

import lombok.*;

import org.hibernate.envers.Audited;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@Audited
public class LedgerDispatchReceipt {

    @Nullable
    private String primaryBlockchainType;

    @Nullable
    private String primaryBlockchainHash;
}
