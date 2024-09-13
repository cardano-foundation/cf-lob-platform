package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FatalError;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReconcilationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReconcilationEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionReconcilationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TransactionReconcilationServiceTest {

    @Mock
    private TransactionReconcilationRepository transactionReconcilationRepository;

    @Mock
    private TransactionRepositoryGateway transactionRepositoryGateway;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    private Clock clock = Clock.systemUTC();

    @InjectMocks
    private TransactionReconcilationService transactionReconcilationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        String reconcilationId = "123";
        ReconcilationEntity reconcilationEntity = new ReconcilationEntity();
        when(transactionReconcilationRepository.findById(reconcilationId)).thenReturn(Optional.of(reconcilationEntity));

        Optional<ReconcilationEntity> result = transactionReconcilationService.findById(reconcilationId);

        assertTrue(result.isPresent());
        assertEquals(reconcilationEntity, result.get());
    }

    @Test
    void testCreateReconcilation() {
        String reconcilationId = "123";
        String organisationId = "org1";
        String adapterInstanceId = "adapter1";
        String initiator = "user";
        LocalDate from = LocalDate.now(clock);
        LocalDate to = LocalDate.now(clock).plusDays(1);

        transactionReconcilationService.createReconcilation(reconcilationId, organisationId, from, to);

        ArgumentCaptor<ReconcilationEntity> captor = ArgumentCaptor.forClass(ReconcilationEntity.class);
        verify(transactionReconcilationRepository).save(captor.capture());
        ReconcilationEntity savedEntity = captor.getValue();

        assertEquals(reconcilationId, savedEntity.getId());
        assertEquals(organisationId, savedEntity.getOrganisationId());
        assertEquals(ReconcilationStatus.CREATED, savedEntity.getStatus());
        assertEquals(from, savedEntity.getFrom().orElseThrow());
        assertEquals(to, savedEntity.getTo().orElseThrow());

        verify(applicationEventPublisher).publishEvent(any(ReconcilationCreatedEvent.class));
    }

    @Test
    void testFailReconcilation() {
        String reconcilationId = "123";
        String organisationId = "org1";
        LocalDate from = LocalDate.now(clock);
        LocalDate to = LocalDate.now(clock).plusDays(1);
        FatalError error = new FatalError(FatalError.Code.ADAPTER_ERROR, "SubCode", null);

        when(transactionReconcilationRepository.findById(reconcilationId)).thenReturn(Optional.empty());

        transactionReconcilationService.failReconcilation(reconcilationId, organisationId, Optional.of(from), Optional.of(to), error);

        ArgumentCaptor<ReconcilationEntity> captor = ArgumentCaptor.forClass(ReconcilationEntity.class);
        verify(transactionReconcilationRepository).save(captor.capture());
        ReconcilationEntity failedEntity = captor.getValue();

        assertEquals(reconcilationId, failedEntity.getId());
        assertEquals(ReconcilationStatus.FAILED, failedEntity.getStatus());
    }

    @Test
    void testReconcileChunk_ReconcilationNotFound() {
        String reconcilationId = "123";
        String organisationId = "org1";
        LocalDate fromDate = LocalDate.now(clock);
        LocalDate toDate = LocalDate.now(clock).plusDays(1);
        Set<TransactionEntity> detachedChunkTxs = Set.of();

        when(transactionReconcilationRepository.findById(reconcilationId)).thenReturn(Optional.empty());

        transactionReconcilationService.reconcileChunk(reconcilationId, organisationId, fromDate, toDate, detachedChunkTxs);

        verify(transactionReconcilationRepository, atLeastOnce()).save(any());
        verify(transactionRepositoryGateway, never()).findByAllId(anySet());
    }

    @Test
    void testWrapUpReconcilation_ReconcilationNotFound() {
        String reconcilationId = "123";
        String organisationId = "org1";

        when(transactionReconcilationRepository.findById(reconcilationId)).thenReturn(Optional.empty());

        transactionReconcilationService.wrapUpReconcilation(reconcilationId, organisationId);

        verify(transactionReconcilationRepository, atLeastOnce()).save(any());
    }

    @Test
    void testWrapUpReconcilation_AlreadyCompleted() {
        String reconcilationId = "123";
        String organisationId = "org1";
        ReconcilationEntity reconcilationEntity = new ReconcilationEntity();
        reconcilationEntity.setStatus(ReconcilationStatus.COMPLETED);

        when(transactionReconcilationRepository.findById(reconcilationId)).thenReturn(Optional.of(reconcilationEntity));

        transactionReconcilationService.wrapUpReconcilation(reconcilationId, organisationId);

        verify(transactionReconcilationRepository, never()).save(any());
    }

}
