package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

public enum ReconcilationRejectionCode {

    SOURCE_RECONCILATION_FAIL, // transaction version will have to be ignored because it cannot be extracted again
    SINK_RECONCILATION_FAIL, // blockchain has different version of the transaction but why?
    SOURCE_AND_SINK_RECONCILATION_FAIL, // both source and sink have different version of the transaction

    TX_NOT_IN_ERP, // transaction is not in ERP, typically this is a technical error
    TX_NOT_IN_LOB // transaction is not in LOB, typically it has not been imported yet at all

}
