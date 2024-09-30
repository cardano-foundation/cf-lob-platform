CREATE TABLE blockchain_reader_transaction (
   transaction_id CHAR(64) NOT NULL,
   organisation_id CHAR(64) NOT NULL,
   absolute_slot BIGINT NOT NULL,
   tx_hash CHAR(64) NOT NULL,

   created_by VARCHAR(255),
   updated_by VARCHAR(255),

   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE,

   PRIMARY KEY (transaction_id)
);
