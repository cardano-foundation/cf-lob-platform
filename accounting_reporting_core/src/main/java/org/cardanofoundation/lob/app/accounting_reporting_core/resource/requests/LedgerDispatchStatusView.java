package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

/**
 * To create a filter useful for UI to get batches that includes transactions with specific combinations of statuses.
 */
public enum LedgerDispatchStatusView {
    APPROVE, /** Ready to approve */

    PENDING, /** when exist a violation or a rejection related to LOB problem */

    INVALID, /** when exist a violation or a rejection related to ERP problem*/

    PUBLISH, /** Ready to published */

    PUBLISHED, /** Sent to the published */

    DISPATCHED ; /** DISPATCHED, COMPLETED or FINALIZED status */


}
