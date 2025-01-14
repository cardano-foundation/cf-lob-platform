package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;

@Slf4j
@RequiredArgsConstructor
public class ExtractionParametersFilteringService {

    public Set<Transaction> applyExtractionParameters(Set<Transaction> txs,
                                                      UserExtractionParameters userExtractionParameters,
                                                      SystemExtractionParameters systemExtractionParameters) {
        return txs.stream()
                // this is for sanity reasons since actually we should be filtering out the transactions in the adapter layer and more specifically while making an HTTP call via NetSuiteClient
                .filter(tx -> {
                    val txAccountingPeriod = tx.getAccountingPeriod(); // e.g. 2023-01
                    val txAccountPeriodBeginning = txAccountingPeriod.atDay(1); // e.g. 2023-01-01
                    val txAccountPeriodEnd = txAccountingPeriod.atEndOfMonth(); // e.g. 2023-01-31

                    return (txAccountPeriodBeginning.equals(systemExtractionParameters.getAccountPeriodFrom()) || txAccountPeriodBeginning.isAfter(systemExtractionParameters.getAccountPeriodFrom()))
                            &&
                            (txAccountPeriodEnd.equals(systemExtractionParameters.getAccountPeriodTo()) || txAccountPeriodEnd.isBefore(systemExtractionParameters.getAccountPeriodTo()));
                })
                .filter(tx -> userExtractionParameters.getOrganisationId().equals(tx.getOrganisation().getId()))
                // this is for sanity reasons since actually we should be filtering out the transactions in the adapter layer and more specifically while making an HTTP call via NetSuiteClient
                .filter(tx -> {
                    val from = userExtractionParameters.getFrom();

                    return tx.getEntryDate().isEqual(from) || tx.getEntryDate().isAfter(from);
                })
                // this is for sanity reasons since actually we should be filtering out the transactions in the adapter layer and more specifically while making an HTTP call via NetSuiteClient
                .filter(tx -> {
                    val to = userExtractionParameters.getTo();

                    return tx.getEntryDate().isEqual(to) || tx.getEntryDate().isBefore(to);
                })
                .filter(tx -> {
                    val txTypes = userExtractionParameters.getTransactionTypes();

                    return txTypes.isEmpty() || txTypes.contains(tx.getTransactionType());
                })
                .filter(tx -> {
                    val transactionNumbers = userExtractionParameters.getTransactionNumbers();

                    return transactionNumbers.isEmpty() || transactionNumbers.contains(tx.getInternalTransactionNumber());
                })
                .collect(Collectors.toSet());
    }

    public Set<Transaction> applyExtractionParameters(Set<Transaction> txs,
                                                      String organisationId,
                                                      LocalDate from,
                                                      LocalDate to) {
        return txs.stream()
                .filter(tx -> organisationId.equals(tx.getOrganisation().getId()))
                // this is for sanity reasons since actually we are filtering out the transactions in the adapter layer and more specifically while making an HTTP call via NetSuiteClient
                .filter(tx -> tx.getEntryDate().isEqual(from) || tx.getEntryDate().isAfter(from))
                // this is for sanity reasons since actually we are filtering out the transactions in the adapter layer and more specifically while making an HTTP call via NetSuiteClient
                .filter(tx -> tx.getEntryDate().isEqual(to) || tx.getEntryDate().isBefore(to))
                .collect(Collectors.toSet());
    }

}
