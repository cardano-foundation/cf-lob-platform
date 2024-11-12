package org.cardano.foundation.lob.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class L1SubmissionData {
    String txHash;
    long absoluteSlot;
}
