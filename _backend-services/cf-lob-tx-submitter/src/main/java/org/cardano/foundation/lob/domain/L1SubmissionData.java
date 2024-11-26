package org.cardano.foundation.lob.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class L1SubmissionData {
    String txHash;
    long absoluteSlot;
}
