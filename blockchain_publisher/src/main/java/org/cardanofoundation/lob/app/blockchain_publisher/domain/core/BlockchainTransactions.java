package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionEntity;

import java.util.Set;

public record BlockchainTransactions(String organisationId,
                                     Set<TransactionEntity> submittedTransactions,
                                     Set<TransactionEntity> remainingTransactions,
                                     long creationSlot,
                                     byte[] serialisedTxData) {
}
