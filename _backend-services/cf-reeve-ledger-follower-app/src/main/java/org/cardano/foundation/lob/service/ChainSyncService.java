package org.cardano.foundation.lob.service;

import org.cardano.foundation.lob.domain.SyncStatus;

public interface ChainSyncService {

    SyncStatus getSyncStatus(boolean cached);

    static class Noop implements ChainSyncService {

        @Override
        public SyncStatus getSyncStatus(boolean cached) {
            return SyncStatus.ok(0);
        }
    }

}
