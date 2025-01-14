package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.Report;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.ReportEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service("blockchain_publisher.blockchainPublisherService")
@RequiredArgsConstructor
@Slf4j
public class BlockchainPublisherService {

    private final TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;
    private final ReportEntityRepositoryGateway reportEntityRepositoryGateway;
    private final LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;
    private final TransactionConverter transactionConverter;
    private final ReportConverter reportConverter;

    @Transactional
    public void storeTransactionForDispatchLater(String organisationId,
                                                 Set<Transaction> txs) {
        log.info("dispatchTransactionsToBlockchains..., orgId:{}", organisationId);

        val txEntities = txs.stream()
                .map(transactionConverter::convertToDbDetached)
                .collect(Collectors.toSet());

        val storedTransactions = transactionEntityRepositoryGateway.storeOnlyNew(txEntities);

        ledgerUpdatedEventPublisher.sendTxLedgerUpdatedEvents(organisationId, storedTransactions);
    }

    @Transactional
    public void storeReportsForDispatchLater(String organisationId,
                                             Set<Report> reports) {
        log.info("storeReportsForDispatchLater..., orgId:{}", organisationId);

        val storedReports = reportEntityRepositoryGateway.storeOnlyNew(reports.stream()
                .map(reportConverter::convertToDbDetached)
                .collect(Collectors.toSet()));

        ledgerUpdatedEventPublisher.sendReportLedgerUpdatedEvents(organisationId, storedReports);
    }

}
