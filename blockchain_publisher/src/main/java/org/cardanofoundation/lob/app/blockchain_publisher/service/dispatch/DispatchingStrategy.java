package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionEntity;

import java.util.Set;
import java.util.function.BiFunction;

public interface DispatchingStrategy extends BiFunction<String, Set<TransactionEntity>, Set<TransactionEntity>> {

    default Set<TransactionEntity> apply(String organisationId, Set<TransactionEntity> transactions) {
        return transactions;
    }

}
