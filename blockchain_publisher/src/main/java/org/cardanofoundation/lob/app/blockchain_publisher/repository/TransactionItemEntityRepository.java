package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionItemEntity;

public interface TransactionItemEntityRepository extends JpaRepository<TransactionItemEntity, String> {

}
