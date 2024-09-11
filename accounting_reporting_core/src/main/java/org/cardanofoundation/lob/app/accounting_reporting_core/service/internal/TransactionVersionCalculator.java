package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.support.calc.BigDecimals;
import org.cardanofoundation.lob.app.support.crypto.SHA3;

import java.util.Comparator;

@Slf4j
public class TransactionVersionCalculator {

    public static String compute(Source algo,
                                 TransactionEntity transactionEntity) {
        val b = new StringBuilder();

        b.append(transactionEntity.getId());
        b.append(transactionEntity.getTransactionInternalNumber());
        b.append(compute(algo, transactionEntity.getOrganisation()));
        b.append(transactionEntity.getTransactionType());
        b.append(transactionEntity.getEntryDate());

        // to be on the safe side lets sort this in hash calculation logic
        val predictablySortedTxItems = transactionEntity.getItems()
                .stream()
                .sorted(Comparator.comparing(TransactionItemEntity::getId))
                .toList();

        for (val item : predictablySortedTxItems) {
            b.append(compute(algo, item));
        }

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(Source algo, TransactionItemEntity item) {
        val b = new StringBuilder();

        b.append(item.getId());

        item.getAccountCredit().ifPresent(acc -> b.append(compute(algo, acc)));
        item.getAccountDebit().ifPresent(acc -> b.append(compute(algo, acc)));

        b.append(BigDecimals.normalise(item.getFxRate()));

        b.append(BigDecimals.normalise(item.getAmountFcy()));
        b.append(BigDecimals.normalise(item.getAmountLcy()));

        item.getCostCenter().ifPresent(cc -> b.append(compute(algo, cc)));
        item.getProject().ifPresent(p -> b.append(compute(algo, p)));
        item.getDocument().ifPresent(d -> b.append(compute(algo, d)));

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(Source algo, Document document) {
        val b = new StringBuilder();

        b.append(document.getNum());

        document.getCounterparty().ifPresent(cp -> b.append(compute(algo, cp)));
        document.getVat().ifPresent(v -> b.append(compute(algo, v)));
        b.append(document.getCurrency().getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(Source algo, Vat vat) {
        val b = new StringBuilder();

        b.append(vat.getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(Source algo, Counterparty counterparty) {
        val b = new StringBuilder();

        b.append(counterparty.getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(Source algo, CostCenter costCenter) {
        val b = new StringBuilder();

        b.append(costCenter.getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(Source algo, Project project) {
        val b = new StringBuilder();

        b.append(project.getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(Source algo, Organisation org) {
        val b = new StringBuilder();

        b.append(org.getId());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(Source algo, Account acc) {
        val b = new StringBuilder();

        b.append(acc.getCode());

        return SHA3.digestAsHex(b.toString());
    }

}
