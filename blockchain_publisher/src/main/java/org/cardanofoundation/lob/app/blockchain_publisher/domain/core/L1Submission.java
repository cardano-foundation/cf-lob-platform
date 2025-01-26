package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import java.util.Optional;

public record L1Submission(String txHash,
                           Optional<Long> absoluteSlot,
                           boolean confirmed) { }
