package org.cardanofoundation.lob.app.blockchain_reader.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@Slf4j
@Getter
@EqualsAndHashCode
public class LOBOnChainBatch {

    private String organisationId;

    @Builder.Default
    private Set<LOBOnChainTransaction> transactions = new LinkedHashSet<>();

}
