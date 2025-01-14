package org.cardanofoundation.lob.app.blockchain_reader.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LOBOnChainTxStatusRequest {

    private Set<String> transactionIds = new LinkedHashSet<String>();

}
