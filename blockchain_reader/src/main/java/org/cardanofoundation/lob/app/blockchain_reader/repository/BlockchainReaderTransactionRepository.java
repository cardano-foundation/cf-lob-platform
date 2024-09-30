package org.cardanofoundation.lob.app.blockchain_reader.repository;

import org.cardanofoundation.lob.app.blockchain_reader.domain.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockchainReaderTransactionRepository extends JpaRepository<TransactionEntity, String> {
}
