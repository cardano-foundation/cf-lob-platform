package org.cardanofoundation.lob.app.blockchain_publisher.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(value = "lob.blockchain.publisher.enabled", havingValue = "true")
public class TransactionsWatchDogJob {

    // TODO

}
