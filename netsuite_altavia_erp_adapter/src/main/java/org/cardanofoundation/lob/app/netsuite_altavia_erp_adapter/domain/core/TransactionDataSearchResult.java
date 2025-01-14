package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionDataSearchResult(List<TxLine> lines,
                                          boolean more) {
}
