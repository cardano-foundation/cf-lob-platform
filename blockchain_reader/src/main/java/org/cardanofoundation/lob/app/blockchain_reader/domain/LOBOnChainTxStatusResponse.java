package org.cardanofoundation.lob.app.blockchain_reader.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LOBOnChainTxStatusResponse {

    @NotNull
    private Map<String, Boolean> transactionStatuses = new LinkedHashMap<>();

    @NotNull
    private CardanoNetwork network;

}
