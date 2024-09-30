package org.cardanofoundation.lob.app.blockchain_reader.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class LOBOnChainTransaction {

    private String id;

}
