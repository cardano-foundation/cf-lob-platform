package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEntityRepositoryGateway {

    private final TransactionEntityRepository transactionEntityRepository;
    private final TransactionItemEntityRepository transactionItemEntityRepository;

    /**
     * Store only new transactions. We want our interface to be idempotent so if somebody sents the same transaction
     * we will ignore it.
     *
     * @param transactionEntities
     * @return stored transactions
     */
    @Transactional
    public Set<TransactionEntity> storeOnlyNewTransactions(Set<TransactionEntity> transactionEntities) {
        log.info("StoreOnlyNewTransactions..., storeOnlyNewTransactions:{}", transactionEntities.size());

        val txIds = transactionEntities.stream()
                .map(TransactionEntity::getId)
                .collect(toSet());

        val existingTransactions = transactionEntityRepository
                .findAllById(txIds)
                .stream()
                .collect(toSet());

        val newTransactions = Sets.difference(transactionEntities, existingTransactions);

        val newTxs = Stream.concat(transactionEntityRepository.saveAll(newTransactions)
                        .stream(), existingTransactions.stream())
                .collect(toSet());

        for (val tx : newTxs) {
            transactionItemEntityRepository.saveAll(tx.getItems());
        }

        return newTxs;
    }

}
