package org.cardanofoundation.lob.app.blockchain_publisher.job;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.BlockchainPublisherException;
import org.cardanofoundation.lob.app.blockchain_publisher.service.TransactionsWatchDogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(value = "lob.blockchain.publisher.watchdog.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TransactionsWatchDogJob {

    private final TransactionsWatchDogService transactionsWatchDogService;

    @Value("${lob.blockchain.publisher.watchdog.tx.limit.per.org.pull.size:1000}")
    private int txStatusInspectionLimitPerOrgPullSize = 1000; // limit per org in one go as in, per one job run

    @PostConstruct
    public void init() {
        log.info("TransactionsWatchDogJob is enabled.");
    }

    @Scheduled(
            fixedDelayString = "${lob.blockchain.publisher.watchdog.fixedDelay:PT1M}",
            initialDelayString = "${lob.blockchain.publisher.watchdog.initialDelay:PT1M}"
    )
    public void execute() {
        log.info("Inspecting all organisations transactions for on chain transaction status changes...");

        val organisationResultsE = transactionsWatchDogService.checkTransactionStatusesForOrganisation(txStatusInspectionLimitPerOrgPullSize);

        for (val orgResult : organisationResultsE) {
            if (orgResult.isLeft()) {
                throw new BlockchainPublisherException(STR."Failed to check transaction statuses for organisation., title:\{orgResult.getLeft().getTitle()}, msg:\{orgResult.getLeft().getDetail()}");
            }
        }

        for (val orgResult : organisationResultsE) {
            if (orgResult.isRight()) {
                log.info(STR."Number of transactions for org with tx count: \{orgResult.get().size()}");
            }
        }

        log.info("Inspection of all organisations transactions completed.");
    }

}
