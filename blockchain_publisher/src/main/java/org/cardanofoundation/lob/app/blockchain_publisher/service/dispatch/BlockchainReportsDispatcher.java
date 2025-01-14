package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.SUBMITTED;

import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bloxbean.cardano.client.api.exception.ApiException;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.ReportEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.ReportSerializer;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.TransactionSubmissionService;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainReportsDispatcher {

    private final OrganisationPublicApi organisationPublicApi;
    private final ReportEntityRepositoryGateway reportEntityRepositoryGateway;
    private final DispatchingStrategy<ReportEntity> dispatchingStrategy = new ImmediateDispatchingStrategy<>();
    private final ReportSerializer reportSerializer;
    private final TransactionSubmissionService transactionSubmissionService;
    private final LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;

    @Value("${lob.blockchain_publisher.dispatcher.pullBatchSize:50}")
    private int pullTransactionsBatchSize = 50;

    @Transactional
    public void dispatchReports() {
        log.info("Pooling for blockchain reports to be send to the blockchain...");

        for (val organisation : organisationPublicApi.listAll()) {
            val organisationId = organisation.getId();

            val reports = reportEntityRepositoryGateway.findReportsByStatus(organisationId, pullTransactionsBatchSize);
            val reportsCount = reports.size();

            log.info("Dispatching reports for organisationId: {}, report count:{}", organisationId, reportsCount);

            if (reportsCount > 0) {
                val toDispatch = dispatchingStrategy.apply(organisationId, reports);

                dispatchReports(organisationId, toDispatch);
            }
        }

        log.info("Pooling for blockchain reports to be send to the blockchain...done");
    }

    @Transactional
    protected void dispatchReports(String organisationId,
                                   Set<ReportEntity> reportEntities) {
        log.info("Dispatching reports for organisation: {}", organisationId);

        for (val reportEntity : reportEntities) {
            dispatchReport(organisationId, reportEntity);
        }
    }

    @Transactional
    public void dispatchReport(String organisationId, ReportEntity reportEntity) {
        log.info("Dispatching report for organisation: {}", organisationId);

        try {
            sendReportOnChainAndUpdateDb(reportEntity);
        } catch (InterruptedException | ApiException e) {
            log.error("Error sending report on chain and / or updating db", e);
        }
    }

    @Transactional
    private void sendReportOnChainAndUpdateDb(ReportEntity reportEntity) throws InterruptedException, ApiException {
        log.info("Sending report on chain and updating db, reportId:{}", reportEntity.getReportId());

        val reportTxData = reportSerializer.serialize();
        if (reportTxData.length > 0) {
            val l1SubmissionData = transactionSubmissionService.submitTransactionWithPossibleConfirmation(reportTxData);

            val txHash = l1SubmissionData.txHash();
            val txAbsoluteSlotM = l1SubmissionData.absoluteSlot();

            updateTransactionStatuses(txHash, txAbsoluteSlotM, reportEntity);
            ledgerUpdatedEventPublisher.sendReportLedgerUpdatedEvents(reportEntity.getOrganisation().getId(), Set.of(reportEntity));

            log.info("Blockchain transaction submitted (report), l1SubmissionData:{}", l1SubmissionData);
        }

        log.info("No report data to send to blockchain, since tx building failed.");
    }

    @Transactional
    private void updateTransactionStatuses(String txHash,
                                           Optional<Long> absoluteSlot,
                                           ReportEntity reportEntity) {
        reportEntity.setL1SubmissionData(Optional.of(L1SubmissionData.builder()
                    .transactionHash(txHash)
                    .absoluteSlot(absoluteSlot.orElse(null)) // if tx is not confirmed yet, slot will not be available
                    .creationSlot(1L) // TODO find out the right creation slot
                    .publishStatus(SUBMITTED)
                    .build())
            );

        reportEntityRepositoryGateway.storeReport(reportEntity);
    }

}
