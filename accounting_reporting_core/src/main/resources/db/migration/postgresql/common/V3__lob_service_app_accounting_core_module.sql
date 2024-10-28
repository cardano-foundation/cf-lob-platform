CREATE TABLE IF NOT EXISTS accounting_core_transaction_batch (
   transaction_batch_id CHAR(64) NOT NULL,

   status VARCHAR(255) NOT NULL,

   stats_total_transactions_count INT,
   stats_processed_transactions_count INT,
   stats_failed_transactions_count INT,
   stats_approved_transactions_count INT,
   stats_approved_transactions_dispatch_count INT,
   stats_dispatched_transactions_count INT,
   stats_completed_transactions_count INT,
   stats_finalized_transactions_count INT,
   stats_failed_source_erp_transactions_count INT,
   stats_failed_source_lob_transactions_count INT,

   detail_code VARCHAR(255),
   detail_subcode VARCHAR(255),
   detail_bag jsonb,

   filtering_parameters_organisation_id VARCHAR(255) NOT NULL,
   filtering_parameters_transaction_types SMALLINT,
   filtering_parameters_transaction_numbers text[] NOT NULL DEFAULT '{}',
   filtering_parameters_from_date DATE NOT NULL,
   filtering_parameters_to_date DATE NOT NULL,
   filtering_parameters_accounting_period_from DATE, -- NULLable since sometimes when batch fails we do not have it
   filtering_parameters_accounting_period_to DATE, -- NULLable since sometimes when batch fails we do not have it

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_batch_id)
);

CREATE TABLE IF NOT EXISTS accounting_core_transaction_batch_aud (
    transaction_batch_id CHAR(64) NOT NULL,

    status VARCHAR(255) NOT NULL,

    stats_total_transactions_count INT,
    stats_processed_transactions_count INT,
    stats_failed_transactions_count INT,
    stats_approved_transactions_count INT,
    stats_approved_transactions_dispatch_count INT,
    stats_dispatched_transactions_count INT,
    stats_completed_transactions_count INT,
    stats_finalized_transactions_count INT,
    stats_failed_source_erp_transactions_count INT,
    stats_failed_source_lob_transactions_count INT,

    detail_code VARCHAR(255),
    detail_subcode VARCHAR(255),
    detail_bag JSONB,

    filtering_parameters_organisation_id VARCHAR(255) NOT NULL,
    filtering_parameters_transaction_types SMALLINT,
    filtering_parameters_transaction_numbers text[] NOT NULL DEFAULT '{}',
    filtering_parameters_from_date DATE NOT NULL,
    filtering_parameters_to_date DATE NOT NULL,
    filtering_parameters_accounting_period_from DATE,
    filtering_parameters_accounting_period_to DATE,

    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,

    -- Special columns for audit tables
    rev INTEGER NOT NULL,
    revtype SMALLINT NOT NULL,

    -- Primary Key for the audit table
    CONSTRAINT pk_accounting_core_transaction_batch_aud PRIMARY KEY (transaction_batch_id, rev, revtype),

    -- Foreign Key to the revision information table
    FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS accounting_core_transaction (
   transaction_id CHAR(64) NOT NULL,
   type VARCHAR(255) NOT NULL,
   batch_id CHAR(64) NOT NULL,

   FOREIGN KEY (batch_id) REFERENCES accounting_core_transaction_batch (transaction_batch_id),

   entry_date DATE NOT NULL,
   accounting_period CHAR(7) NOT NULL,
   transaction_internal_number VARCHAR(255) NOT NULL,

   organisation_id CHAR(64) NOT NULL,
   organisation_name VARCHAR(255),
   organisation_country_code VARCHAR(255),
   organisation_tax_id_number VARCHAR(255),
   organisation_currency_id VARCHAR(255),

   reconcilation_id CHAR(64),

   reconcilation_source VARCHAR(255),
   reconcilation_sink VARCHAR(255),
   reconcilation_final_status VARCHAR(255),

   user_comment VARCHAR(255),

   automated_validation_status VARCHAR(255) NOT NULL,

   transaction_approved BOOLEAN NOT NULL,
   ledger_dispatch_approved BOOLEAN NOT NULL,
   ledger_dispatch_status VARCHAR(255) NOT NULL,

   overall_status VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_id)
);

CREATE TABLE IF NOT EXISTS accounting_core_transaction_aud (
   transaction_id CHAR(64) NOT NULL,
   type VARCHAR(255) NOT NULL,
   batch_id CHAR(64) NOT NULL,

   FOREIGN KEY (batch_id) REFERENCES accounting_core_transaction_batch (transaction_batch_id),

   entry_date DATE NOT NULL,
   accounting_period CHAR(7) NOT NULL,
   transaction_internal_number VARCHAR(255) NOT NULL,

   organisation_id CHAR(64) NOT NULL,
   organisation_name VARCHAR(255),
   organisation_country_code VARCHAR(255),
   organisation_tax_id_number VARCHAR(255),
   organisation_currency_id VARCHAR(255),

   reconcilation_id CHAR(64),

   reconcilation_source VARCHAR(255),
   reconcilation_sink VARCHAR(255),
   reconcilation_final_status VARCHAR(255),

   user_comment VARCHAR(255),

   automated_validation_status VARCHAR(255) NOT NULL,

   transaction_approved BOOLEAN NOT NULL,
   ledger_dispatch_approved BOOLEAN NOT NULL,
   ledger_dispatch_status VARCHAR(255) NOT NULL,

   overall_status VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- special for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT NOT NULL,

   -- Primary Key Constraint
   CONSTRAINT pk_accounting_core_transaction_aud PRIMARY KEY (transaction_id, rev, revtype),

   -- Foreign Key Constraint to revinfo table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- embeddable
CREATE TABLE IF NOT EXISTS accounting_core_transaction_violation (
   transaction_id CHAR(64) NOT NULL,
   tx_item_id CHAR(64),
   code VARCHAR(255) NOT NULL,

   severity VARCHAR(255) NOT NULL,
   sub_code VARCHAR(255) NOT NULL,
   source VARCHAR(255) NOT NULL,
   processor_module VARCHAR(255) NOT NULL,
   detail_bag jsonb NOT NULL,

   CONSTRAINT fk_accounting_core_transaction_violation FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id),

   UNIQUE (transaction_id, tx_item_id, code, sub_code)
);

-- embeddable audit table for transaction violation
CREATE TABLE IF NOT EXISTS accounting_core_transaction_violation_aud (
    transaction_id CHAR(64) NOT NULL,
    tx_item_id CHAR(64),
    code VARCHAR(255) NOT NULL,

    severity VARCHAR(255) NOT NULL,
    sub_code VARCHAR(255) NOT NULL,
    source VARCHAR(255) NOT NULL,
    processor_module VARCHAR(255) NOT NULL,
    detail_bag JSONB NOT NULL,

    -- Special columns for audit tables
    rev INTEGER NOT NULL,
    revtype SMALLINT NOT NULL,
    ord INTEGER,

    CONSTRAINT pk_accounting_core_transaction_violation_aud UNIQUE (transaction_id, tx_item_id, code, sub_code, rev, revtype),

    -- Foreign Key referencing the original transaction table
    FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION,

    -- Foreign Key to the revision information table
    FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS accounting_core_transaction_batch_assoc (
   transaction_batch_id CHAR(64) NOT NULL,
   transaction_id CHAR(64) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   FOREIGN KEY (transaction_batch_id) REFERENCES accounting_core_transaction_batch (transaction_batch_id),
   FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id),

   PRIMARY KEY (transaction_batch_id, transaction_id)
);

CREATE TABLE IF NOT EXISTS accounting_core_transaction_batch_assoc_aud (
    transaction_batch_id CHAR(64) NOT NULL,
    transaction_id CHAR(64) NOT NULL,

    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,

    -- Special columns for audit tables
    rev INTEGER NOT NULL,
    revtype SMALLINT NOT NULL,
    ord INTEGER,

    -- Primary Key for the audit table
    CONSTRAINT pk_accounting_core_transaction_batch_assoc_aud PRIMARY KEY (transaction_batch_id, transaction_id, rev, revtype),

    -- Foreign Key referencing the transaction batch table
    FOREIGN KEY (transaction_batch_id) REFERENCES accounting_core_transaction_batch (transaction_batch_id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION,

    -- Foreign Key referencing the transaction table
    FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION,

    -- Foreign Key to the revision information table
    FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS accounting_core_transaction_item (
   transaction_item_id CHAR(64) NOT NULL,

   transaction_id CHAR(64) NOT NULL,

   FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id),

   fx_rate DECIMAL(12, 8) NOT NULL,

   rejection_reason SMALLINT,

   account_code_debit VARCHAR(255),
   account_ref_code_debit VARCHAR(255),
   account_name_debit VARCHAR(255),

   account_code_credit VARCHAR(255),
   account_ref_code_credit VARCHAR(255),
   account_name_credit VARCHAR(255),

   account_event_code VARCHAR(255),
   account_event_name VARCHAR(255),

   amount_fcy DECIMAL(100, 8) NOT NULL,
   amount_lcy DECIMAL(100, 8) NOT NULL,

   document_num VARCHAR(255),
   document_currency_customer_code VARCHAR(255),
   document_currency_id VARCHAR(255),

   document_vat_customer_code VARCHAR(255),
   document_vat_rate DECIMAL(12, 8),

   document_counterparty_customer_code VARCHAR(255),
   document_counterparty_type VARCHAR(255),
   document_counterparty_name VARCHAR(255),

   project_customer_code VARCHAR(255),
   project_external_customer_code VARCHAR(255),
   project_name VARCHAR(255),

   cost_center_customer_code VARCHAR(255),
   cost_center_external_customer_code VARCHAR(255),
   cost_center_name VARCHAR(255),

   status VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_item_id)
);

CREATE TABLE IF NOT EXISTS accounting_core_transaction_item_aud (
   transaction_item_id CHAR(64) NOT NULL,

   transaction_id CHAR(64) NOT NULL,

   -- Foreign key referencing transaction_id in accounting_core_transaction
   FOREIGN KEY (transaction_id) REFERENCES accounting_core_transaction (transaction_id),

   fx_rate DECIMAL(12, 8) NOT NULL,

   rejection_reason SMALLINT,

   account_code_debit VARCHAR(255),
   account_ref_code_debit VARCHAR(255),
   account_name_debit VARCHAR(255),

   account_code_credit VARCHAR(255),
   account_ref_code_credit VARCHAR(255),
   account_name_credit VARCHAR(255),

   account_event_code VARCHAR(255),
   account_event_name VARCHAR(255),

   amount_fcy DECIMAL(100, 8) NOT NULL,
   amount_lcy DECIMAL(100, 8) NOT NULL,

   document_num VARCHAR(255),
   document_currency_customer_code VARCHAR(255),
   document_currency_id VARCHAR(255),

   document_vat_customer_code VARCHAR(255),
   document_vat_rate DECIMAL(12, 8),

   document_counterparty_customer_code VARCHAR(255),
   document_counterparty_type VARCHAR(255),
   document_counterparty_name VARCHAR(255),

   project_customer_code VARCHAR(255),
   project_external_customer_code VARCHAR(255),
   project_name VARCHAR(255),

   cost_center_customer_code VARCHAR(255),
   cost_center_external_customer_code VARCHAR(255),
   cost_center_name VARCHAR(255),
   status VARCHAR(255) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   -- Special columns for audit tables
   rev INTEGER NOT NULL,
   revtype SMALLINT NOT NULL,
   ord INTEGER,

   -- Primary Key for audit table
   CONSTRAINT pk_accounting_core_transaction_item_aud PRIMARY KEY (transaction_item_id, rev, revtype),

   -- Foreign Key to the revision information table
   FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
   ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS accounting_core_reconcilation (
    reconcilation_id CHAR(64) NOT NULL,
    organisation_id CHAR(64) NOT NULL,
    from_date DATE,
    to_date DATE,
    status VARCHAR(255) NOT NULL,

    processed_tx_count INT NOT NULL,

    detail_code VARCHAR(255),
    detail_subcode VARCHAR(255),
    detail_bag jsonb,

    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,

    PRIMARY KEY (reconcilation_id)
);

CREATE TABLE IF NOT EXISTS accounting_core_reconcilation_aud (
    reconcilation_id CHAR(64) NOT NULL,
    organisation_id CHAR(64) NOT NULL,
    from_date DATE,
    to_date DATE,
    status VARCHAR(255) NOT NULL,

    processed_tx_count INT NOT NULL,

    detail_code VARCHAR(255),
    detail_subcode VARCHAR(255),
    detail_bag JSONB,

    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,

    -- Special columns for audit tables
    rev INTEGER NOT NULL,
    revtype SMALLINT NOT NULL,
    ord INTEGER,

    -- Primary Key for the audit table
    CONSTRAINT pk_accounting_core_reconcilation_aud PRIMARY KEY (reconcilation_id, rev, revtype),

    -- Foreign Key to revision information table
    FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- embeddable
CREATE TABLE IF NOT EXISTS accounting_core_reconcilation_violation (
    reconcilation_id CHAR(64) NOT NULL,
    transaction_id CHAR(64) NOT NULL,
    rejection_code VARCHAR(255) NOT NULL,

    transaction_internal_number VARCHAR(255) NOT NULL,
    transaction_entry_date DATE NOT NULL,
    transaction_type VARCHAR(255) NOT NULL,
    amount_lcy_sum DECIMAL(100, 8) NOT NULL,

    source_diff jsonb,

    CONSTRAINT fk_accounting_core_reconcilation_violation FOREIGN KEY (reconcilation_id) REFERENCES accounting_core_reconcilation (reconcilation_id),

    PRIMARY KEY (reconcilation_id, transaction_id, rejection_code)
);

-- embeddable audit table for reconciliation violation
CREATE TABLE IF NOT EXISTS accounting_core_reconcilation_violation_aud (
    reconcilation_id CHAR(64) NOT NULL,
    transaction_id CHAR(64) NOT NULL,
    rejection_code VARCHAR(255) NOT NULL,
    transaction_internal_number VARCHAR(255) NOT NULL,
    transaction_entry_date DATE NOT NULL,
    transaction_type VARCHAR(255) NOT NULL,
    amount_lcy_sum DECIMAL(100, 8) NOT NULL,
    source_diff JSONB,

    -- Special columns for audit tables
    rev INTEGER NOT NULL,
    revtype SMALLINT NOT NULL,
    ord INTEGER,

    -- Primary Key for the audit table
    CONSTRAINT pk_accounting_core_reconcilation_violation_aud PRIMARY KEY (reconcilation_id, transaction_id, rejection_code, rev, revtype),

    -- Foreign Key referencing the original reconciliation table
    FOREIGN KEY (reconcilation_id) REFERENCES accounting_core_reconcilation (reconcilation_id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION,

    -- Foreign Key to revision information table
    FOREIGN KEY (rev) REFERENCES revinfo (rev) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- indices

-- Indexes for JSONB columns
CREATE INDEX idx_transaction_violation_bag_gin
ON accounting_core_transaction_violation USING GIN (detail_bag);

-- Indexes for JSONB columns
CREATE INDEX idx_transaction_batch_bag_gin
ON accounting_core_transaction_batch USING GIN (detail_bag);

-- Indexes for JSONB columns
CREATE INDEX idx_accounting_core_reconcilation_bag_gin
ON accounting_core_reconcilation USING GIN (detail_bag);

-- Indexes for text array
CREATE INDEX idx_transaction_batch_filtering_transaction_numbers
ON accounting_core_transaction_batch USING GIN (filtering_parameters_transaction_numbers);

-- AccountingCoreTransactionRepository.findDispatchableTransactions
CREATE INDEX idx_transaction_dispatchable
ON accounting_core_transaction (organisation_id, overall_status, ledger_dispatch_status, created_at, transaction_id);

-- AccountingCoreTransactionRepository.findByEntryDateRangeAndNotReconciledYet
CREATE INDEX idx_transaction_reconciliation_status
ON accounting_core_transaction (organisation_id, entry_date, reconcilation_source, reconcilation_sink, created_at, transaction_id);

-- CustomTransactionRepository.findAllByStatus
CREATE INDEX idx_transaction_organisation_status
ON accounting_core_transaction (organisation_id, automated_validation_status);

CREATE INDEX idx_transaction_reconcilation_id
ON accounting_core_transaction (reconcilation_id);

-- CustomTransactionBatchRepositoryImpl.findByFilter
CREATE INDEX idx_reconcilation_violation_source
ON accounting_core_transaction (reconcilation_source);

-- CustomTransactionBatchRepositoryImpl.findByFilter
CREATE INDEX idx_transaction_status
ON accounting_core_transaction (overall_status);

-- CustomTransactionBatchRepositoryImpl.queryCriteria
CREATE INDEX idx_transaction_approval_dispatch_validation_status
ON accounting_core_transaction (transaction_approved, ledger_dispatch_approved, automated_validation_status);

-- CustomTransactionBatchRepositoryImpl.findByFilter
CREATE INDEX idx_transaction_batch
ON accounting_core_transaction_batch (filtering_parameters_organisation_id, filtering_parameters_transaction_types, created_at);

-- AccountingCoreTransactionRepository.findAllByTxId
CREATE INDEX idx_transaction_batch_assoc_transaction_id
ON accounting_core_transaction_batch_assoc (transaction_id);

-- TransactionItemRepository.findByTxIdAndItemId
CREATE INDEX idx_transaction_item_txid_txitemid
ON accounting_core_transaction_item (transaction_id, transaction_item_id, rejection_reason);

-- CustomTransactionRepository.findAllReconciliation
CREATE INDEX idx_reconcilation_final_status
ON accounting_core_reconcilation (status);

-- CustomTransactionRepository.findAllReconciliationSpecial
CREATE INDEX idx_reconcilation_violation_rejection_code
ON accounting_core_reconcilation_violation (rejection_code);

CREATE INDEX idx_reconcilation_violation_transaction_id
ON accounting_core_reconcilation_violation (transaction_id);
