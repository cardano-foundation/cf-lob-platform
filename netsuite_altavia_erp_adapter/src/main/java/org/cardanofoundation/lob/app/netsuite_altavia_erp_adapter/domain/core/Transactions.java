package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core;

import java.util.Set;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;

public record Transactions(String organisationId,
                           Set<Transaction> transactions) {
}
