package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchAssocEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface TransactionBatchAssocRepository extends JpaRepository<TransactionBatchAssocEntity, TransactionBatchAssocEntity.Id> {

    @Query("SELECT tbe FROM accounting_reporting_core.TransactionBatchAssocEntity tbe WHERE tbe.id.transactionId = :txId")
    Set<TransactionBatchAssocEntity> findAllByTxId(@Param("txId") String txId);

}
