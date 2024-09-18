package org.cardanofoundation.lob.app.blockchain_reader.service;

import org.cardanofoundation.lob.app.blockchain_reader.domain.SyncStatus;

public interface ChainSyncService {

    SyncStatus getSyncStatus(boolean cached);

    static class Noop implements ChainSyncService {

        @Override
        public SyncStatus getSyncStatus(boolean cached) {
            return SyncStatus.ok(0);
        }
    }

}
