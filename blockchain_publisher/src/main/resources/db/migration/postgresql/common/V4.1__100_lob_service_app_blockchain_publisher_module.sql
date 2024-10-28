CREATE TABLE blockchain_publisher_transaction (
   transaction_id CHAR(64) NOT NULL,
   organisation_id VARCHAR(255) NOT NULL, -- consider moving this to accounting_core_batch
   internal_number VARCHAR(255) NOT NULL,
   batch_id CHAR(64) NOT NULL,

   organisation_name VARCHAR(255) NOT NULL,
   organisation_tax_id_number VARCHAR(255) NOT NULL,
   organisation_country_code VARCHAR(2) NOT NULL,
   organisation_currency_id VARCHAR(255) NOT NULL,

   type VARCHAR(255) NOT NULL,
   accounting_period CHAR(7) NOT NULL,
   entry_date DATE NOT NULL,

   l1_finality_score VARCHAR(255),
   l1_transaction_hash CHAR(64),
   l1_absolute_slot BIGINT,
   l1_creation_slot BIGINT,
   l1_publish_status VARCHAR(255),

   created_by VARCHAR(255),
   updated_by VARCHAR(255),

   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_id)
);

CREATE TABLE blockchain_publisher_transaction_item (
   transaction_item_id CHAR(64) NOT NULL,

   transaction_id CHAR(64) NOT NULL,

   FOREIGN KEY (transaction_id) REFERENCES blockchain_publisher_transaction (transaction_id),

   fx_rate DECIMAL(12, 8) NOT NULL,

   amount_fcy DECIMAL(100, 8) NOT NULL,

   account_event_code VARCHAR(255) NOT NULL,
   account_event_name VARCHAR(255) NOT NULL,

   project_customer_code VARCHAR(255),
   project_name VARCHAR(255),

   cost_center_customer_code VARCHAR(255),
   cost_center_name VARCHAR(255),

   document_num VARCHAR(255),
   document_currency_id VARCHAR(255) NOT NULL,
   document_currency_customer_code VARCHAR(255),
   document_counterparty_customer_code VARCHAR(255),
   document_counterparty_type VARCHAR(255),

   document_vat_customer_code VARCHAR(255),
   document_vat_rate DECIMAL(12, 8),

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_item_id)
);

-- blockchain_publisher.TransactionEntity.findTransactionsByStatus
CREATE INDEX idx_transaction_entity_publish_status
ON blockchain_publisher_transaction (organisation_id, l1_publish_status, created_at, transaction_id);
