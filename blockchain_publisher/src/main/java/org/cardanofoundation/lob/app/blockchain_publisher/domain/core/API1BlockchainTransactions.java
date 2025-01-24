package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionEntity;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public record API1BlockchainTransactions(String organisationId,
                                         Set<TransactionEntity> submittedTransactions,
                                         Set<TransactionEntity> remainingTransactions,
                                         long creationSlot,
                                         byte[] serialisedTxData,
                                         String receiverAddress) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        API1BlockchainTransactions that = (API1BlockchainTransactions) o;

        return creationSlot == that.creationSlot &&
                Objects.equals(organisationId, that.organisationId) &&
                Objects.equals(submittedTransactions, that.submittedTransactions) &&
                Objects.equals(remainingTransactions, that.remainingTransactions) &&
                Arrays.equals(serialisedTxData, that.serialisedTxData)
                && receiverAddress.equals(that.receiverAddress)
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(organisationId, submittedTransactions, remainingTransactions, creationSlot);
        result = 31 * result + Arrays.hashCode(serialisedTxData);
        result = 31 * result + Objects.hashCode(receiverAddress);

        return result;
    }

}
