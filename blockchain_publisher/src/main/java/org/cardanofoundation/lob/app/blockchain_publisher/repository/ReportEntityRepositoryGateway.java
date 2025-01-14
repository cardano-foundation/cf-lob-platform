package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import static java.util.stream.Collectors.toSet;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportEntityRepositoryGateway {

    private final ReportEntityRepository reportEntityRepository;

    @Transactional
    public Set<ReportEntity> storeOnlyNew(Set<ReportEntity> reportEntities) {
        log.info("Store only new..., store only new: {}", reportEntities.size());

        val reportIds = reportEntities.stream()
                .map(ReportEntity::getId)
                .collect(toSet());

        val existingReports = reportEntityRepository
                .findAllById(reportIds)
                .stream()
                .collect(toSet());

        val newReports = Sets.difference(reportEntities, existingReports);

        return reportEntityRepository.saveAll(newReports)
                .stream()
                .collect(toSet());
    }

    @Transactional
    public Set<ReportEntity> findReportsByStatus(String organisationId,
                                                 int pullReportsBatchSize) {
        val dispatchStatuses = BlockchainPublishStatus.toDispatchStatuses();
        val limit = Limit.of(pullReportsBatchSize);

        return reportEntityRepository.findReportsByStatus(organisationId, dispatchStatuses, limit);
    }

    @Transactional
    public void storeReport(ReportEntity reportEntity) {
        reportEntityRepository.save(reportEntity);
    }

}
