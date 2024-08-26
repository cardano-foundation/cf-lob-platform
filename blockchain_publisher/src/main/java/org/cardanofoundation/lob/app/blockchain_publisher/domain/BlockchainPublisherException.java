package org.cardanofoundation.lob.app.blockchain_publisher.domain;

public class BlockchainPublisherException extends RuntimeException {

    public BlockchainPublisherException(String message) {
        super(message);
    }

    public BlockchainPublisherException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockchainPublisherException(Throwable cause) {
        super(cause);
    }
}
