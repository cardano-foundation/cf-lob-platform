package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

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
     * Store only new transaction otherwise return empty
     *
     * @param transactionEntity
     * @return maybe stored transaction entity
     */
    @Transactional
    public Optional<TransactionEntity> storeOnlyNewTransaction(TransactionEntity transactionEntity) {
        log.info("Store only new transaction: " + transactionEntity.getId());

        val existingTransactionM = transactionEntityRepository
                .findById(transactionEntity.getId());

        if (existingTransactionM.isPresent()) {
            return Optional.empty();
        }

        val newTx = transactionEntityRepository.save(transactionEntity);

        transactionItemEntityRepository.saveAll(transactionEntity.getItems());

        return Optional.of(newTx);
    }

    @Transactional
    public void storeTransaction(TransactionEntity transactionEntity) {
        transactionEntityRepository.save(transactionEntity);
    }

}
