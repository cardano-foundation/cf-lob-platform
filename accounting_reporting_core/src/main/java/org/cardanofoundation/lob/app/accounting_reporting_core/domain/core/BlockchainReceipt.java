package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BlockchainReceipt {

    private String type;

    private String hash;

}
