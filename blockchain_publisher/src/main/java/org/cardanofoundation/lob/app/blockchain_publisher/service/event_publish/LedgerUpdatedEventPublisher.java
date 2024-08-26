package org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainPublishStatusMapper;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerUpdatedEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    @Value("${lob.blockchain_publisher.send.batch.size:100}")
    protected int dispatchBatchSize = 100;

    @Transactional
    public void sendLedgerUpdatedEvents(String organisationId,
                                        Set<TransactionEntity> allTxs) {
        log.info("Sending ledger updated event for organisation:{}, submittedTransactions:{}", organisationId, allTxs.size());

        val partitions = Partitions.partition(allTxs, dispatchBatchSize);

        for (val partition : partitions) {
            val txStatuses = partition.asSet().stream()
                    .map(txEntity -> {
                        val publishStatusM = txEntity.getL1SubmissionData().flatMap(L1SubmissionData::getPublishStatus);
                        val cardanoFinalityScoreM = txEntity.getL1SubmissionData().flatMap(L1SubmissionData::getFinalityScore);
                        val ledgerDispatchStatus = blockchainPublishStatusMapper.convert(publishStatusM, cardanoFinalityScoreM);

                        return new TxStatusUpdate(txEntity.getId(), ledgerDispatchStatus);
                    })
                    .collect(Collectors.toSet());

            log.info("Sending ledger updated event for organisation:{}, statuses:{}", organisationId, txStatuses);

            applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(organisationId, txStatuses));
        }
    }

}
