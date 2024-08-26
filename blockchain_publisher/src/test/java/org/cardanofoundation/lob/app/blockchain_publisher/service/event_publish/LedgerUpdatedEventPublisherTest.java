package org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoFinalityScore;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainPublishStatusMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LedgerUpdatedEventPublisherTest {

    private LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    @Captor
    private ArgumentCaptor<LedgerUpdatedEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ledgerUpdatedEventPublisher = new LedgerUpdatedEventPublisher(applicationEventPublisher, blockchainPublishStatusMapper);
    }

    @Test
    void testSendLedgerUpdatedEvents_NoTransactions() {
        Set<TransactionEntity> emptyTransactions = Set.of();

        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents("org1", emptyTransactions);

        verify(applicationEventPublisher, never()).publishEvent(any(LedgerUpdatedEvent.class));
    }

    @Test
    void testSendLedgerUpdatedEvents_SingleTransaction() {
        TransactionEntity transaction = createTransactionEntity(BlockchainPublishStatus.SUBMITTED, CardanoFinalityScore.HIGH);
        when(blockchainPublishStatusMapper.convert(any(Optional.class), any(Optional.class)))
                .thenReturn(LedgerDispatchStatus.COMPLETED);

        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents("org1", Set.of(transaction));

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        LedgerUpdatedEvent event = eventCaptor.getValue();

        assertThat(event.getOrganisationId()).isEqualTo("org1");
        assertThat(event.getStatusUpdates()).hasSize(1);

        TxStatusUpdate statusUpdate = event.getStatusUpdates().iterator().next();
        assertThat(statusUpdate.getTxId()).isEqualTo(transaction.getId());
        assertThat(statusUpdate.getStatus()).isEqualTo(LedgerDispatchStatus.COMPLETED);
    }

    @Test
    void testSendLedgerUpdatedEvents_MultipleTransactions_SingleBatch() {
        TransactionEntity transaction1 = createTransactionEntity(BlockchainPublishStatus.STORED, CardanoFinalityScore.MEDIUM);
        TransactionEntity transaction2 = createTransactionEntity(BlockchainPublishStatus.ROLLBACKED, CardanoFinalityScore.LOW);
        when(blockchainPublishStatusMapper.convert(any(Optional.class), any(Optional.class)))
                .thenReturn(LedgerDispatchStatus.DISPATCHED);

        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents("org1", Set.of(transaction1, transaction2));

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        LedgerUpdatedEvent event = eventCaptor.getValue();

        assertThat(event.getOrganisationId()).isEqualTo("org1");
        assertThat(event.getStatusUpdates()).hasSize(2);
    }

    @Test
    void testSendLedgerUpdatedEvents_MultipleTransactions_MultipleBatches() {
        ledgerUpdatedEventPublisher.dispatchBatchSize = 1; // Set batch size to 1 for testing

        TransactionEntity transaction1 = createTransactionEntity(BlockchainPublishStatus.STORED, CardanoFinalityScore.MEDIUM);
        TransactionEntity transaction2 = createTransactionEntity(BlockchainPublishStatus.ROLLBACKED, CardanoFinalityScore.LOW);
        when(blockchainPublishStatusMapper.convert(any(Optional.class), any(Optional.class)))
                .thenReturn(LedgerDispatchStatus.DISPATCHED);

        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents("org1", Set.of(transaction1, transaction2));

        verify(applicationEventPublisher, times(2)).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).hasSize(2);
    }

    @Test
    void testSendLedgerUpdatedEvents_PublishStatusEmpty() {
        TransactionEntity transaction = createTransactionEntity(null, CardanoFinalityScore.HIGH);
        when(blockchainPublishStatusMapper.convert(any(Optional.class), any(Optional.class)))
                .thenReturn(LedgerDispatchStatus.NOT_DISPATCHED);

        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents("org1", Set.of(transaction));

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        LedgerUpdatedEvent event = eventCaptor.getValue();

        assertThat(event.getOrganisationId()).isEqualTo("org1");
        assertThat(event.getStatusUpdates()).hasSize(1);

        TxStatusUpdate statusUpdate = event.getStatusUpdates().iterator().next();
        assertThat(statusUpdate.getTxId()).isEqualTo(transaction.getId());
        assertThat(statusUpdate.getStatus()).isEqualTo(LedgerDispatchStatus.NOT_DISPATCHED);
    }

    @Test
    void testSendLedgerUpdatedEvents_FinalityScoreEmpty() {
        TransactionEntity transaction = createTransactionEntity(BlockchainPublishStatus.STORED, null);
        when(blockchainPublishStatusMapper.convert(any(Optional.class), any(Optional.class)))
                .thenReturn(LedgerDispatchStatus.MARK_DISPATCH);

        ledgerUpdatedEventPublisher.sendLedgerUpdatedEvents("org1", Set.of(transaction));

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        LedgerUpdatedEvent event = eventCaptor.getValue();

        assertThat(event.getOrganisationId()).isEqualTo("org1");
        assertThat(event.getStatusUpdates()).hasSize(1);

        TxStatusUpdate statusUpdate = event.getStatusUpdates().iterator().next();
        assertThat(statusUpdate.getTxId()).isEqualTo(transaction.getId());
        assertThat(statusUpdate.getStatus()).isEqualTo(LedgerDispatchStatus.MARK_DISPATCH);
    }

    private TransactionEntity createTransactionEntity(@Nullable BlockchainPublishStatus publishStatus,
                                                      @Nullable CardanoFinalityScore finalityScore) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(UUID.randomUUID().toString());

        L1SubmissionData submissionData = new L1SubmissionData();
        submissionData.setPublishStatus(Optional.ofNullable(publishStatus));
        submissionData.setFinalityScore(Optional.ofNullable(finalityScore));

        transactionEntity.setL1SubmissionData(Optional.of(submissionData));

        return transactionEntity;
    }

}