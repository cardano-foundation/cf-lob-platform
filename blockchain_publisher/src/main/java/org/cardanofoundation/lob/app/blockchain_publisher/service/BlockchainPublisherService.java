package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service("blockchain_publisher.blockchainPublisherService")
@RequiredArgsConstructor
@Slf4j
public class BlockchainPublisherService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;
    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    @Value("${lob.blockchain_publisher.send.batch.size:100}")
    private final int dispatchBatchSize = 100;

    @Transactional
    public void storeTransactionForDispatchLater(String organisationId,
                                                 Set<TransactionEntity> transactions) {
        log.info("dispatchTransactionsToBlockchains..., orgId:{}", organisationId);

        val storedTransactions = transactionEntityRepositoryGateway.storeOnlyNewTransactions(transactions);

        notifyTransactionStored(organisationId, storedTransactions);
    }

    @Transactional
    private void notifyTransactionStored(String organisationId,
                                         Set<TransactionEntity> txs) {
        val partitions = Partitions.partition(txs, dispatchBatchSize);

        for (val partition : partitions) {
            val txStatusUpdates = partition.asSet().stream()
                    .map(txEntity -> {
                        val assuranceLevelM = txEntity.getL1SubmissionData()
                                .flatMap(L1SubmissionData::getAssuranceLevel);

                        val blockchainPublishStatusM = txEntity.getL1SubmissionData().flatMap(L1SubmissionData::getPublishStatus);
                        val status = blockchainPublishStatusMapper.convert(blockchainPublishStatusM, assuranceLevelM);
                        val txId = txEntity.getId();

                        return new TxStatusUpdate(txId, status);
                    })
                    .collect(Collectors.toSet());

            applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(organisationId, txStatusUpdates));
        }
    }

}
