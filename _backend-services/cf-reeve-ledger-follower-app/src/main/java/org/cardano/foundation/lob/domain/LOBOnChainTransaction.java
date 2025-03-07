package org.cardano.foundation.lob.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class LOBOnChainTransaction {

    private String id;

}
