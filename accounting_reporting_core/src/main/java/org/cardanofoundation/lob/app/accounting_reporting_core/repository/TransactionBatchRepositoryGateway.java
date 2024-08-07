package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionBatchRepositoryGateway {

    private final TransactionBatchRepository transactionBatchRepository;

    public Optional<TransactionBatchEntity> findById(String batchId) {
        return transactionBatchRepository.findById(batchId);
    }

    // TODO: Pagination need to be implemented
    public List<TransactionBatchEntity> findByOrganisationId(String organisationId) {
        return transactionBatchRepository.findAllByFilteringParametersOrganisationId(organisationId);
    }

    public List<TransactionBatchEntity> findByFilter(BatchSearchRequest body) {
        return transactionBatchRepository.findByFilter(body);
    }

    public Long findByFilterCount(BatchSearchRequest body) {
        return transactionBatchRepository.findByFilterCount(body);
    }

}
