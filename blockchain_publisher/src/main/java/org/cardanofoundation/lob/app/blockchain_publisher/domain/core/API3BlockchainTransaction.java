package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;

public record API3BlockchainTransaction(ReportEntity report,
                                        long creationSlot,
                                        byte[] serialisedTxData) {
}
