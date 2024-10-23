package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public enum TxItemValidationStatus {

    OK,
    ERASED_SELF_PAYMENT,
    ERASED_SUM_APPLIED,
    ERASED_ZERO_BALANCE;

    public boolean isErased() {
        return this != OK;
    }

    public boolean isOK() {
        return this == OK;
    }

}
