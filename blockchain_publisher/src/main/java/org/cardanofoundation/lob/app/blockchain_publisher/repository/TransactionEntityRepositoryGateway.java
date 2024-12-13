package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionEntity;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.notFinalisedButVisibleOnChain;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionEntityRepositoryGateway {

    private final TransactionEntityRepository transactionEntityRepository;
    private final TransactionItemEntityRepository transactionItemEntityRepository;

    public Optional<TransactionEntity> findById(String txId) {
        return transactionEntityRepository.findById(txId);
    }

    public Set<TransactionEntity> findTransactionsByStatus(String organisationId, int pullTransactionsBatchSize) {
        val dispatchStatuses = BlockchainPublishStatus.toDispatchStatuses();
        val limit = Limit.of(pullTransactionsBatchSize);

        return transactionEntityRepository.findTransactionsByStatus(organisationId, dispatchStatuses, limit);
    }

    public Set<TransactionEntity> findDispatchedTransactionsThatAreNotFinalizedYet(String organisationId, Limit limit) {
        val notFinalisedButVisibleOnChain = notFinalisedButVisibleOnChain();

        return transactionEntityRepository.findDispatchedTransactionsThatAreNotFinalizedYet(organisationId, notFinalisedButVisibleOnChain, limit);
    }


    /**
     * Store only new transactions. We want our interface to be idempotent so if somebody sents the same transaction
     * we will ignore it.
     *
     * @param transactionEntities
     * @return stored transactions
     */
    @Transactional
    public Set<TransactionEntity> storeOnlyNew(Set<TransactionEntity> transactionEntities) {
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

    @Transactional
    public void storeTransaction(TransactionEntity transactionEntity) {
        transactionEntityRepository.save(transactionEntity);
    }

}
