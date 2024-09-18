package org.cardanofoundation.lob.app.blockchain_reader.domain;

import java.util.Optional;

public record SyncStatus(boolean isSynced, Optional<Long> diff, Optional<Exception> ex) {

    public static SyncStatus ok(long diff) {
        return new SyncStatus(true, Optional.of(diff), Optional.empty());
    }

    public static SyncStatus notYet(long diff) {
        return new SyncStatus(false, Optional.of(diff), Optional.empty());
    }

    public static SyncStatus notYet() {
        return new SyncStatus(false, Optional.empty(), Optional.empty());
    }

    public static SyncStatus error(Exception ex) {
        return new SyncStatus(false, Optional.empty(), Optional.of(ex));
    }

    public static SyncStatus unknownError() {
        return new SyncStatus(false, Optional.empty(), Optional.empty());
    }

}