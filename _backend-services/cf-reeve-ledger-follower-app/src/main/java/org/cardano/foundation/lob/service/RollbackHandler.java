package org.cardano.foundation.lob.service;

import com.bloxbean.cardano.yaci.store.events.BlockEvent;
import com.bloxbean.cardano.yaci.store.events.RollbackEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "rollback.handling", value = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class RollbackHandler {

    @Value("${rollback.handling.enabled:true}")
    private boolean isEnabled;

    private boolean initialized = false;

    private final TransactionService transactionService;

    @PostConstruct
    public void init() {
        if (!isEnabled) {
            log.info("Rollback handling is disabled. Skipping rollback handler / sync...");
        } else {
            log.info("Rollback handling is enabled. Starting rollback handler / sync...");
        }
    }

    @EventListener
    public void onBlock(BlockEvent blockEvent) {
        if (!initialized) {
            log.info("Block sync init.");
            initialized = true;
        }
    }

    @EventListener
    public void onRollback(RollbackEvent rollbackEvent) {
        log.info("Rollback detected. Handling rollback...");
        if (!initialized) {
            log.info("Rollback detected before block sync initialization. Skipping rollback handling...");
            return;
        }

        log.info("Rolling back to slot: {}", rollbackEvent.getRollbackTo().getSlot());
        transactionService.deleteAfterSlot(rollbackEvent.getRollbackTo().getSlot());
    }

}
