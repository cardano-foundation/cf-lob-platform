package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReconcilationRejectionCode;

public enum ReconciliationRejectionCodeRequest {

    MISSING_IN_ERP,
    IN_PROCESSING,
    NEW_IN_ERP,
    NEW_VERSION_NOT_PUBLISHED,
    NEW_VERSION;

    public static ReconciliationRejectionCodeRequest of(ReconcilationRejectionCode code) {
        return switch (code) {
            case SOURCE_RECONCILATION_FAIL -> ReconciliationRejectionCodeRequest.NEW_VERSION;
            case SINK_RECONCILATION_FAIL -> ReconciliationRejectionCodeRequest.IN_PROCESSING;
            case TX_NOT_IN_ERP -> ReconciliationRejectionCodeRequest.MISSING_IN_ERP;
            case TX_NOT_IN_LOB -> ReconciliationRejectionCodeRequest.NEW_IN_ERP;
        };
    }
}