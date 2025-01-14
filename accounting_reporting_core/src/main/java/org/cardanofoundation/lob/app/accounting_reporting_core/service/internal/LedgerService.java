package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReportStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.report.ReportEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.ReportLedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TransactionLedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreTransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.ReportRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.cardanofoundation.lob.app.support.modulith.EventMetadata;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final AccountingCoreTransactionRepository accountingCoreTransactionRepository;
    private final ReportRepository reportRepository;
    private final ReportConverter reportConverter;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionConverter transactionConverter;
    private final PIIDataFilteringService piiDataFilteringService;
    private final OrganisationPublicApi organisationPublicApi;

    @Value("${ledger.dispatch.batch.size:100}")
    private int dispatchBatchSize;

    @Transactional
    public void updateTransactionsWithNewStatuses(Map<String, TxStatusUpdate> statuses) {
        log.info("Updating dispatch status for statusMapCount: {}", statuses.size());

        val txIds = statuses.keySet();

        val transactionEntities = accountingCoreTransactionRepository.findAllById(txIds);

        for (val tx : transactionEntities) {
            val txStatusUpdate = statuses.get(tx.getId());
            tx.setLedgerDispatchStatus(txStatusUpdate.getStatus());
        }

        accountingCoreTransactionRepository.saveAll(transactionEntities);

        log.info("Updated dispatch status for statusMapCount: {} completed.", statuses.size());
    }

    @Transactional
    public void updateReportsWithNewStatuses(Map<String, ReportStatusUpdate> reportStatusUpdateMap) {
        log.info("Updating dispatch status for statusMapCount: {}", reportStatusUpdateMap.size());

        val reportIds = reportStatusUpdateMap.keySet();

        val reports = reportRepository.findAllById(reportIds);

        for (val report : reports) {
            val reportsLedgerUpdatedEvent = reportStatusUpdateMap.get(report.getId());
            report.setLedgerDispatchStatus(reportsLedgerUpdatedEvent.getStatus());
        }

        reportRepository.saveAll(reports);

        log.info("Updated dispatch status for statusMapCount: {} completed.", reportStatusUpdateMap.size());
    }

    @Transactional
    public void dispatchPending(int limit) {
        for (val organisation : organisationPublicApi.listAll()) {
            val dispatchTransactions = accountingCoreTransactionRepository.findDispatchableTransactions(organisation.getId(), Limit.of(limit));
            val dispatchReports = reportRepository.findDispatchableTransactions(organisation.getId(), Limit.of(limit));

            log.info("dispatchPending transactions and reports, organisationId: {}, total tx count: {}", organisation.getId(), dispatchTransactions.size());

            dispatchPendingTransactions(organisation.getId(), dispatchTransactions);
            dispatchReports(organisation.getId(), dispatchReports);
        }
    }

    @Transactional(propagation = SUPPORTS)
    private void dispatchPendingTransactions(String organisationId,
                                             Set<TransactionEntity> transactions) {
        log.info("dispatchTransactionToBlockchainPublisher, total tx count: {}", transactions.size());

        if (transactions.isEmpty()) {
            log.info("dispatchPendingTransactions, no transactions to dispatch.");
            return;
        }

        val canonicalTxs = transactionConverter.convertFromDb(transactions);
        val piiFilteredOutTransactions = piiDataFilteringService.apply(canonicalTxs);

        for (val partition : Partitions.partition(piiFilteredOutTransactions, dispatchBatchSize)) {
            val txs = partition.asSet();

            log.info("dispatchTransactionToBlockchainPublisher, txs, partitionSize: {}", txs.size());

            applicationEventPublisher.publishEvent(TransactionLedgerUpdateCommand.create(
                    EventMetadata.create(TransactionLedgerUpdateCommand.VERSION),
                    organisationId,
                    txs)
            );
        }
    }

    @Transactional(propagation = SUPPORTS)
    public void dispatchReports(String organisationId,
                                Set<ReportEntity> reportEntities) {
        log.info("dispatchReports, total reports count: {}", reportEntities.size());

        if (reportEntities.isEmpty()) {
            log.info("dispatchReports, no reports to dispatch.");
            return;
        }

        val canonicalReports = reportConverter.convertFromDbToCanonicalForm(reportEntities);
        for (val partition : Partitions.partition(canonicalReports, dispatchBatchSize)) {
            val reports = partition.asSet();

            log.info("dispatchTransactionToBlockchainPublisher, reports, reportsCount: {}", reports.size());

            applicationEventPublisher.publishEvent(ReportLedgerUpdateCommand.create(
                    EventMetadata.create(ReportLedgerUpdateCommand.VERSION),
                    organisationId,
                    reports)
            );
        }
    }

}
