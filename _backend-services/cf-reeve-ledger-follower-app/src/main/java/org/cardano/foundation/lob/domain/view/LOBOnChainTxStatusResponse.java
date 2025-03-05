package org.cardano.foundation.lob.domain.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardano.foundation.lob.domain.CardanoNetwork;

import java.util.LinkedHashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LOBOnChainTxStatusResponse {

    private Map<String, Boolean> transactionStatuses = new LinkedHashMap<>();

    private CardanoNetwork network;

}
