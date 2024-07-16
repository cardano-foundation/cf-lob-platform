package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

/**
 * To create a filter useful for UI to get batches that includes transactions with specific combinations of statuses.
 */
public enum LedgerDispatchStatusView {
    APPROVE, /** Mark to be dispatched transactions */

    PENDING, /** Not Dispatched transactions */

    INVALID, /** Validation status FAILED */
    
    PUBLISH, /** Dispatched */

    PUBLISHED; /** Completed */

}
