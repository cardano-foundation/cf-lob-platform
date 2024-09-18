package org.cardanofoundation.lob.app.blockchain_common;

public class BlockchainException extends RuntimeException {

    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockchainException(Throwable cause) {
        super(cause);
    }

}
