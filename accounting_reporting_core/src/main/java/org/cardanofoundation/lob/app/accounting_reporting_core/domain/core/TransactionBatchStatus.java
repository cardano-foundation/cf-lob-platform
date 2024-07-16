package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public enum TransactionBatchStatus {

    CREATED, // job created

    PROCESSING, // job is being processed

    FINISHED, // all transactions processed and valid transactions are dispatched to the blockchain(s)

    COMPLETE, // all VALID transactions that passed business validation are settled to the blockchain(s)

    FINALIZED, // all transactions are settled and the transactions are finalized on the blockchain(s)

    FAILED // job failed due to e.g. fatal error in the adapter layer

}
