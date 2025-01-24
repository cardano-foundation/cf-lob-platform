package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import java.util.Arrays;
import java.util.Objects;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;

public record API3BlockchainTransaction(ReportEntity report,
                                        long creationSlot,
                                        byte[] serialisedTxData,
                                        String receiverAddress) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        API3BlockchainTransaction that = (API3BlockchainTransaction) o;

        return creationSlot == that.creationSlot &&
                Objects.equals(report, that.report) &&
                Arrays.equals(serialisedTxData, that.serialisedTxData)
                && Objects.equals(receiverAddress, that.receiverAddress);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(report, creationSlot);
        result = 31 * result + Arrays.hashCode(serialisedTxData);
        result = 31 * result + Objects.hashCode(receiverAddress);

        return result;
    }

}
