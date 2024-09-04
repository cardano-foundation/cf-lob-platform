package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public enum ReconcilationStatus {

    CREATED, // reconcilation is created but not started yet meaning no chunks sending started yet

    STARTED, // reconcilation is started and first chunk event is sent

    FAILED, // reconcilation is failed

    COMPLETED, // reconcilation is completed, we see this on the last chunk event sent

}
