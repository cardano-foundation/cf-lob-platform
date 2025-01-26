package org.cardanofoundation.lob.app.blockchain_reader.domain;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LOBOnChainTxStatusRequest {

    private Set<String> transactionIds = new LinkedHashSet<String>();

}
