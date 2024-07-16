package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class TransactionTypeMapper implements Function<String, Optional<TransactionType>> {

    public Optional<TransactionType> apply(String transType) {
        return switch(transType) {
            case "CardChrg" -> Optional.of(TransactionType.CardCharge);
            case "VendBill" -> Optional.of(TransactionType.VendorBill);
            case "CardRfnd" -> Optional.of(TransactionType.CardRefund);
            case "Journal" -> Optional.of(TransactionType.Journal);
            case "FxReval" -> Optional.of(TransactionType.FxRevaluation);
            case "Transfer" -> Optional.of(TransactionType.Transfer);
            case "CustPymt" -> Optional.of(TransactionType.CustomerPayment);
            case "ExpRept" -> Optional.of(TransactionType.ExpenseReport);
            case "VendPymt" -> Optional.of(TransactionType.VendorPayment);
            case "VendCred" -> Optional.of(TransactionType.BillCredit);
            default -> Optional.empty();
        };
    }

}
