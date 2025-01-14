package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;

import java.util.Set;

public record Transactions(String organisationId,
                           Set<Transaction> transactions) {
}
