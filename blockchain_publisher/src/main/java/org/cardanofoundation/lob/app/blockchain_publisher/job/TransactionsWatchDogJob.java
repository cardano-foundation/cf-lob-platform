package org.cardanofoundation.lob.app.blockchain_publisher.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(value = "lob.blockchain.publisher.enabled", havingValue = "true")
public class TransactionsWatchDogJob {

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT2M")
    public void execute() {
        //log.info("Polling for to check for transaction statuses...");

        // gets transaction lines from the database which have not been finalished yet
        // checks if transaction is finalised.
        // sends event to Accounting Core with update to the transaction lines which changed their status
    }

}
