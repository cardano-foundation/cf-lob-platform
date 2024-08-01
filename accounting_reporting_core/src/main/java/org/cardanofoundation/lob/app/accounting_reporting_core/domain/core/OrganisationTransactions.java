package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper to indicate that all passedTransactions belong to the same organisation
 *
 * @param organisationId
 * @param transactions
 */
public record OrganisationTransactions(
        String organisationId,
        Set<TransactionEntity> transactions
) {

    public static OrganisationTransactions empty(String organisationId) {
        return new OrganisationTransactions(organisationId, new HashSet<>());
    }

}