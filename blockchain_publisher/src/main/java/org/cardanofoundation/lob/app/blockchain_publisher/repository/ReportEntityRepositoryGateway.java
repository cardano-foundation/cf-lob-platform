package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

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

}
