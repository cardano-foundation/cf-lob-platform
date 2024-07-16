package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core;

import java.util.List;

public record TransactionDataSearchResult(List<TxLine> lines,
                                          boolean more) {
}
