package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import java.util.Arrays;
import java.util.Objects;

public record SerializedCardanoL1Transaction(byte[] txBytes,
                                             byte[] metadataCbor,
                                             String metadataJson) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializedCardanoL1Transaction that = (SerializedCardanoL1Transaction) o;

        return Arrays.equals(txBytes, that.txBytes) &&
                Arrays.equals(metadataCbor, that.metadataCbor) &&
                Objects.equals(metadataJson, that.metadataJson);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(metadataJson);
        result = 31 * result + Arrays.hashCode(txBytes);
        result = 31 * result + Arrays.hashCode(metadataCbor);

        return result;
    }

}
