package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ScheduledReconcilationEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.zalando.problem.Status.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionBatchRepository transactionBatchRepository;
    private final ERPIncomingDataProcessor erpIncomingDataProcessor;

    @Transactional
    public void scheduleIngestion(UserExtractionParameters userExtractionParameters) {
        log.info("scheduleIngestion, parameters: {}", userExtractionParameters);

        val event = ScheduledIngestionEvent.builder().metadata(EventMetadata.create(ScheduledIngestionEvent.VERSION))
                .organisationId(userExtractionParameters.getOrganisationId())
                .userExtractionParameters(userExtractionParameters)
                .build();

        applicationEventPublisher.publishEvent(event);
    }

    @Transactional
    public void scheduleReconilation(String organisationId, LocalDate from, LocalDate to) {
        log.info("scheduleReconilation, organisationId: {}, from: {}, to: {}", organisationId, from, to);

        val event = ScheduledReconcilationEvent.builder()
                .organisationId(organisationId)
                .from(from)
                .to(to)
                .metadata(EventMetadata.create(ScheduledReconcilationEvent.VERSION))
                .build();

        applicationEventPublisher.publishEvent(event);
    }

    @Transactional
    public Either<Problem, Boolean> scheduleReIngestionForFailed(String batchId) {
        log.info("scheduleReIngestion..., batchId: {}", batchId);

        val txBatchM = transactionBatchRepository.findById(batchId);
        if (txBatchM.isEmpty()) {
            return Either.left(Problem.builder()
                        .withTitle("TX_BATCH_NOT_FOUND")
                        .withDetail(STR."Transaction batch with id: \{batchId} not found")
                        .withStatus(NOT_FOUND)
                    .build());
        }

        val txBatch = txBatchM.get();

        val txs =  txBatch.getTransactions().stream()
                .filter(tx -> tx.getAutomatedValidationStatus() == FAILED)
                // reprocess only the ones that have not been approved to dispatch yet, actually it is just a sanity check because it should never happen
                // and we should never allow approving failed transactions
                .filter(tx -> !tx.allApprovalsPassedForTransactionDispatch())
                // we are interested only in the ones that have LOB violations (conversion issues) or rejection issues
                .filter(tx -> tx.getViolations().stream().anyMatch(v -> v.getSource() == LOB) || tx.hasAnyRejection())
                .collect(Collectors.toSet());

        if (txs.isEmpty()) {
            return Either.right(true);
        }

        val processorFlags = ProcessorFlags.builder()
                .reprocess(true)
                .build();

        val organisationId = txBatch.getOrganisationId();

        erpIncomingDataProcessor.continueIngestion(organisationId, batchId, txs.size(), txs, processorFlags);

        return Either.right(true);
    }

}
