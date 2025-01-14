package org.cardanofoundation.lob.app.blockchain_publisher.job;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.cardanofoundation.lob.app.blockchain_common.BlockchainException;
import org.cardanofoundation.lob.app.blockchain_publisher.service.TransactionsWatchDogService;

@Service
@Slf4j
@ConditionalOnProperty(value = "lob.blockchain_publisher.watchdog.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TransactionsWatchDogJob {

    private final TransactionsWatchDogService transactionsWatchDogService;

    @Value("${lob.blockchain_publisher.watchdog.tx_limit_per_org_pull_size:1000}")
    private int txStatusInspectionLimitPerOrgPullSize = 1000; // limit per org in one go as in, per one job run

    @PostConstruct
    public void init() {
        log.info("TransactionsWatchDogJob is enabled.");
    }

    @Scheduled(
            fixedDelayString = "${lob.blockchain_publisher.watchdog.fixed_delay:PT1M}",
            initialDelayString = "${lob.blockchain_publisher.watchdog.initial_delay:PT1M}"
    )
    public void execute() {
        log.info("Inspecting all organisations transactions for on chain transaction status changes...");

        val organisationResultsE = transactionsWatchDogService.checkTransactionStatusesForOrganisation(txStatusInspectionLimitPerOrgPullSize);

        for (val orgResult : organisationResultsE) {
            if (orgResult.isLeft()) {
                throw new BlockchainException(STR."Failed to check transaction statuses for organisation., title:\{orgResult.getLeft().getTitle()}, msg:\{orgResult.getLeft().getDetail()}");
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
