package org.cardanofoundation.lob.app.blockchain_publisher.job;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch.BlockchainTransactionsDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("blockchain_publisher.TransactionDispatcherJob")
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "lob.blockchain.publisher.dispatcher.enabled", havingValue = "true")
public class TransactionDispatcherJob {

    private final BlockchainTransactionsDispatcher blockchainTransactionsDispatcher;

    @PostConstruct
    public void init() {
        log.info("TransactionDispatcherJob is enabled.");
    }

    @Scheduled(
            fixedDelayString = "${lob.blockchain.publisher.dispatcher.fixedDelay:PT10S}",
            initialDelayString = "${lob.blockchain.publisher.dispatcher.initialDelay:PT1M}")
    public void execute() {
        log.info("Pooling for blockchain passedTransactions to be send to the blockchain...");

        blockchainTransactionsDispatcher.dispatchTransactions();

        log.info("Pooling for blockchain passedTransactions to be send to the blockchain...done");
    }

}
