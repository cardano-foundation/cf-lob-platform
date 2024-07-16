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

   l1_assurance_level VARCHAR(255),
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

   fx_rate DECIMAL NOT NULL,

   amount_fcy DECIMAL NOT NULL,

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
   document_vat_rate DECIMAL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_item_id)
);

--CREATE TABLE blockchain_publisher_transaction_line_aud (
--   id CHAR(64) NOT NULL,
--   organisation_id VARCHAR(255) NOT NULL,
--   upload_id UUID NOT NULL,
--   transaction_type VARCHAR(255) NOT NULL,
--   entry_date DATE NOT NULL,
--   transaction_internal_number VARCHAR(255),
--   base_currency_id VARCHAR(255),
--   base_currency_internal_code VARCHAR(255) NOT NULL,
--   target_currency_id VARCHAR(255),
--   target_currency_internal_code VARCHAR(255) NOT NULL,
--   fx_rate DECIMAL NOT NULL,
--   document_internal_number VARCHAR(255),
--   vendor_internal_code VARCHAR(255),
--   vat_internal_code VARCHAR(255),
--   vat_rate DECIMAL,
--   publish_status VARCHAR(255) NOT NULL,
--   l1_assurance_level VARCHAR(255),
--   l1_transaction_hash VARCHAR(255),
--   l1_absolute_slot BIGINT,
--
--   amount_fcy DECIMAL NOT NULL,
--   amount_lcy DECIMAL NOT NULL,
--
--   created_by VARCHAR(255),
--   updated_by VARCHAR(255),
--   created_at TIMESTAMP WITHOUT TIME ZONE,
--   updated_at TIMESTAMP WITHOUT TIME ZONE,
--
--   -- special for audit tables
--   rev INTEGER NOT NULL,
--   revtype SMALLINT,
--
--   CONSTRAINT blockchain_transaction_line_aud_pkey PRIMARY KEY (id, rev),
--   CONSTRAINT blockchain_transaction_line_aud_revinfo FOREIGN KEY (rev)
--   REFERENCES revinfo (rev) MATCH SIMPLE
--   ON UPDATE NO ACTION ON DELETE NO ACTION
--);
