package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionDataSearchResult(List<TxLine> lines,
                                          boolean more) {
}
