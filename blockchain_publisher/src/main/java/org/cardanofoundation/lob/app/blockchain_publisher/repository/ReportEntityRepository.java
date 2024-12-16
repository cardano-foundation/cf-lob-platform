package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ReportEntityRepository extends JpaRepository<ReportEntity, String> {

    @Query("SELECT r FROM blockchain_publisher.report.ReportEntity r WHERE r.organisation.id = :organisationId AND r.l1SubmissionData.publishStatus IN :publishStatuses ORDER BY r.createdAt ASC, r.id ASC")
    Set<ReportEntity> findReportsByStatus(@Param("organisationId") String organisationId,
                                          @Param("publishStatuses") Set<BlockchainPublishStatus> publishStatuses,
                                          Limit limit);

//    @Query("SELECT t FROM blockchain_publisher.TransactionEntity t WHERE t.organisation.id = :organisationId AND t.l1SubmissionData.publishStatus IN :publishStatuses AND t.l1SubmissionData is NOT NULL ORDER BY t.createdAt ASC, t.id ASC")
//    Set<TransactionEntity> findDispatchedTransactionsThatAreNotFinalizedYet(@Param("organisationId") String organisationId,
//                                                                            @Param("publishStatuses") Set<BlockchainPublishStatus> publishStatuses,
//                                                                            Limit limit);

}
