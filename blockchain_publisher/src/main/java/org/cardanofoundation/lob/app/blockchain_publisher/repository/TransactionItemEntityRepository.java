package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionItemEntityRepository extends JpaRepository<TransactionItemEntity, String> {

}
