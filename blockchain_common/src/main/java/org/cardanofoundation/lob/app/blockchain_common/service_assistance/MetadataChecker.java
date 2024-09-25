package org.cardanofoundation.lob.app.blockchain_common.service_assistance;

public interface MetadataChecker {

    boolean checkTransactionMetadata(String json);

    class Noop implements MetadataChecker {
        @Override
        public boolean checkTransactionMetadata(String json) {
            return true;
        }
    }

}
