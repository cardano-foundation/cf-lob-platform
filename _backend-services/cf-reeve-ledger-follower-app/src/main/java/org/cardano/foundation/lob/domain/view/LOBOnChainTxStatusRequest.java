package org.cardano.foundation.lob.domain.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LOBOnChainTxStatusRequest {

    private List<String> transactionIds = List.of();

}
