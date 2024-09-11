package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionReconcilationRepository;
import org.javers.core.Javers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionReconcilationService {

    private final TransactionReconcilationRepository transactionReconcilationRepository;
    private final TransactionRepositoryGateway transactionRepositoryGateway;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Javers javers;

    public Optional<ReconcilationEntity> findById(String reconcilationId) {
        return transactionReconcilationRepository.findById(reconcilationId);
    }

    @Transactional
    public void createReconcilation(String reconcilationId,
                                    String organisationId,
                                    String adapterInstanceId,
                                    String initiator,
                                    LocalDate from,
                                    LocalDate to
    ) {
        log.info("Creating transaction reconcilation entity," +
                        " reconcilationId: {}," +
                        " initiator: {}," +
                        " adapterInstanceId: {}," +
                        " from: {}," +
                        " to: {}",
                reconcilationId, initiator, adapterInstanceId, from, to
        );

        val reconcilationEntity = new ReconcilationEntity();
        reconcilationEntity.setId(reconcilationId);
        reconcilationEntity.setOrganisationId(organisationId);
        reconcilationEntity.setStatus(ReconcilationStatus.CREATED);
        reconcilationEntity.setFrom(Optional.of(from));
        reconcilationEntity.setTo(Optional.of(to));
        reconcilationEntity.setViolations(new LinkedHashSet<>());

        transactionReconcilationRepository.save(reconcilationEntity);

        log.info("Reconcilation created, reconcilationId: {}", reconcilationId);

        applicationEventPublisher.publishEvent(ReconcilationCreatedEvent.builder()
                .reconciliationId(reconcilationId)
                .organisationId(organisationId)
                .adapterInstanceId(adapterInstanceId)
                .from(from)
                .to(to)
                .build()
        );
    }

    @Transactional
    public void failReconcilation(String reconcilationId,
                                  String organisationId,
                                  Optional<LocalDate> from,
                                  Optional<LocalDate> to,
                                  FatalError error) {
        log.info("Failing transaction reconcilation entity," +
                        " reconcilationId: {}," +
                        " from: {}," +
                        " to: {}," +
                        " error: {}",
                reconcilationId, from, to, error
        );
        val reconcilationEntityM = transactionReconcilationRepository.findById(reconcilationId);

        var reconcilationEntity = new ReconcilationEntity();
        if (reconcilationEntityM.isPresent()) {
            reconcilationEntity = reconcilationEntityM.orElseThrow();
        } else {
            reconcilationEntity.setOrganisationId(organisationId);
            reconcilationEntity.setId(reconcilationId);
            reconcilationEntity.setFrom(from);
            reconcilationEntity.setTo(to);
        }

        reconcilationEntity.setStatus(ReconcilationStatus.FAILED);
        reconcilationEntity.setDetails(Optional.of(Details.builder()
                .code(error.getCode().name())
                .subCode(error.getSubCode())
                .bag(error.getBag())
                .build())
        );

        transactionReconcilationRepository.save(reconcilationEntity);

        log.info("Reconcilation failed, reconcilationId: {}", reconcilationId);
    }

    @Transactional
    public void reconcileChunk(String reconcilationId,
                               String organisationId,
                               LocalDate fromDate,
                               LocalDate toDate,
                               Set<TransactionEntity> detachedChunkTxs) {
        log.info("Reconciling transactions, reconcilationId: {}, organisation: {}, from: {}, to:{}, size: {}",
                reconcilationId, organisationId, fromDate, toDate, detachedChunkTxs.size()
        );

        // convert detachedChunkTxs to a map so we can easily loop through them
        val detachedChunkTxsMap = detachedChunkTxs.stream()
                .collect(Collectors.toMap(TransactionEntity::getId, tx -> tx));

        val reconcilationEntityM = transactionReconcilationRepository.findById(reconcilationId);
        if (reconcilationEntityM.isEmpty()) {
            log.error("Reconcilation entity not found, reconcilationId: {}", reconcilationId);

            failReconcilation(
                    reconcilationId,
                    organisationId,
                    Optional.of(fromDate),
                    Optional.of(toDate),
                    new FatalError(FatalError.Code.ADAPTER_ERROR, "RECONCILATION_NOT_FOUND", Map.of())
            );

            return;
        }

        val reconcilationEntity = reconcilationEntityM.get();
        val totalProcessed = reconcilationEntity.getProcessedTxCount() + detachedChunkTxs.size();
        reconcilationEntity.setProcessedTxCount(totalProcessed);
        reconcilationEntity.setStatus(ReconcilationStatus.STARTED);

        val attachedTxEntities = transactionRepositoryGateway.findByAllId(
                detachedChunkTxs.stream().map(TransactionEntity::getId).collect(Collectors.toSet())
        );

        val attachedTxIds = attachedTxEntities.stream()
                .map(TransactionEntity::getId)
                .collect(Collectors.toSet());

        // Find transactions in detachedChunkTxs but not in attachedTxEntities
        val transactionsNotInAttached = detachedChunkTxs.stream()
                .filter(tx -> !attachedTxIds.contains(tx.getId()))
                .collect(Collectors.toSet());

        for (val tx : transactionsNotInAttached) {
            log.warn("Transaction not found in LOB DB yet, needs import, transactionId: {}", tx.getId());

            reconcilationEntity.addViolation(ReconcilationViolation.builder()
                    .transactionId(tx.getId())
                    .rejectionCode(ReconcilationRejectionCode.TX_NOT_IN_LOB)
                    .transactionInternalNumber(tx.getTransactionInternalNumber())
                    .build());
        }

        for (val attachedTx : attachedTxEntities) {
            val detachedTx = detachedChunkTxsMap.get(attachedTx.getId()); // detachedTx can never be null since we using detatched tx ids as a way to find our attached txs

            val attachedTxHash = TransactionVersionCalculator.compute(Source.ERP, attachedTx);
            val detachedTxHash = TransactionVersionCalculator.compute(Source.ERP, detachedTx);

            val sourceReconcilationStatus = attachedTxHash.equals(detachedTxHash)
                    ? ReconcilationCode.OK : ReconcilationCode.NOK;

            if (sourceReconcilationStatus == ReconcilationCode.NOK) {
                val sourceDiff = javers.compare(attachedTx, detachedTx);
                val changes = sourceDiff.getChanges();
                val jsonDiff = javers.getJsonConverter().toJson(changes);

                log.warn("Tx source version issue, tx id:{}, txInternalNumber:{}", detachedTx.getId(), detachedTx.getTransactionInternalNumber(), sourceDiff.prettyPrint());

                log.warn("Diff: {}", sourceDiff.prettyPrint());

                reconcilationEntity.addViolation(ReconcilationViolation.builder()
                        .transactionId(attachedTx.getId())
                        .rejectionCode(ReconcilationRejectionCode.SOURCE_RECONCILATION_FAIL)
                        .sourceDiff(jsonDiff)
                        .transactionInternalNumber(attachedTx.getTransactionInternalNumber())
                        .build());
            }

            attachedTx.setReconcilation(Optional.of(Reconcilation.builder()
                    .source(sourceReconcilationStatus)
                    .build())
            );

        }

        // we can only store back the attached transactions, detatched transactions may not be in db
        // hibernate will store all reconcilation status updates
        transactionRepositoryGateway.storeAll(attachedTxEntities);

        log.info("Saving reconcilation entity, reconcilationId: {}", reconcilationEntity.getId());

        transactionReconcilationRepository.save(reconcilationEntity);

        log.info("Finished reconciling transactions.");
    }

    @Transactional
    public void wrapUpReconcilation(String reconcilationId,
                                    String organisationId) {
        log.info("Wrapping up reconcilation, reconcilationId: {}", reconcilationId);

        val reconcilationEntityM = transactionReconcilationRepository.findById(reconcilationId);
        if (reconcilationEntityM.isEmpty()) {
            log.error("Reconcilation entity not found, reconcilationId: {}", reconcilationId);

            failReconcilation(
                    reconcilationId,
                    organisationId,
                    Optional.empty(),
                    Optional.empty(),
                    new FatalError(FatalError.Code.ADAPTER_ERROR, "RECONCILATION_NOT_FOUND", Map.of())
            );

            return;
        }
        val reconcilationEntity = reconcilationEntityM.get();

        if (reconcilationEntity.getStatus() == ReconcilationStatus.COMPLETED) {
            log.warn("Reconcilation already completed, reconcilationId: {}", reconcilationEntity.getId());
            return;
        }

        log.info("Wrapping up reconcilation, reconcilationId: {}", reconcilationEntity.getId());

        val fromDate = reconcilationEntity.getFrom().orElseThrow();
        val toDate = reconcilationEntity.getTo().orElseThrow();

        val missingTxs = transactionRepositoryGateway.findAllByDateRangeAndNotReconciledYet(organisationId, fromDate, toDate);
        // we have some txs which are in LOB (db) but are missing in the ERP (likely some technical defect)

        log.info("Missing txs in ERP but found in LOB, size: {}", missingTxs.size());

        for (val missingTx : missingTxs) {
            log.error("Transaction missing in ERP but was found in the DB, transactionId: {}", missingTx.getId());

            missingTx.setReconcilation(Optional.of(Reconcilation.builder()
                    .source(ReconcilationCode.NOK)
                    .build())
            );

            reconcilationEntity.addViolation(ReconcilationViolation.builder()
                    .transactionId(missingTx.getId())
                    .rejectionCode(ReconcilationRejectionCode.TX_NOT_IN_ERP)
                    .transactionInternalNumber(missingTx.getTransactionInternalNumber())
                    .build()
            );
        }

        transactionRepositoryGateway.storeAll(missingTxs);

        reconcilationEntity.setStatus(ReconcilationStatus.COMPLETED);
    }

}