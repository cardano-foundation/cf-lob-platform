package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.metadata.MetadataMap;

public interface MetadataChecker {

    boolean checkTransactionMetadata(MetadataMap metadata);

    class Noop implements MetadataChecker {
        @Override
        public boolean checkTransactionMetadata(MetadataMap metadata) {
            return true;
        }
    }

}
