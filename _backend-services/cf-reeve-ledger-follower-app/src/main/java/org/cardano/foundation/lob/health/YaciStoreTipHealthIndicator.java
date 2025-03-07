package org.cardano.foundation.lob.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardano.foundation.lob.service.ChainSyncService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("yaciStoreChainSync")
@Slf4j
@RequiredArgsConstructor
public class YaciStoreTipHealthIndicator implements HealthIndicator {

    private final ChainSyncService chainSyncService;

    @Override
    public Health health() {
        var syncStatus = chainSyncService.getSyncStatus(false);

        if (syncStatus.isSynced()) {
            return Health
                    .up()
                    .withDetail("message", "Yaci-Store synced with the original chain!")
                    .withDetail("diffSlots", syncStatus.diff().orElse(-1L))
                    .build();
        }

        if (syncStatus.ex().isPresent()) {
            return Health
                    .down()
                    .withDetail("message", "Yaci-Store error...")
                    .withDetail("diffSlots", syncStatus.diff().orElse(-1L))
                    .withException(syncStatus.ex().get())
                    .build();
        }

        return Health
                .down()
                .withDetail("message", "Yaci-Store is syncing...")
                .withDetail("diffSlots", syncStatus.diff().orElse(-1L))
                .build();
    }

}
