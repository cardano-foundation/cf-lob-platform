package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import java.util.Set;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionEntity;

public record BlockchainTransactions(String organisationId,
                                     Set<TransactionEntity> submittedTransactions,
                                     Set<TransactionEntity> remainingTransactions,
                                     long creationSlot,
                                     byte[] serialisedTxData) {
}
