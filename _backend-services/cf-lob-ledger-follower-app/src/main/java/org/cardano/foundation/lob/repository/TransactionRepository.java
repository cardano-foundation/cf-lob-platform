package org.cardano.foundation.lob.repository;

import org.cardano.foundation.lob.domain.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    @Query("DELETE FROM TransactionEntity t where t.l1AbsoluteSlot > :absoluteSlot")
    @Modifying
    void deleteBySlotGreaterThan(@Param("absoluteSlot") long absoluteSlot);

}
