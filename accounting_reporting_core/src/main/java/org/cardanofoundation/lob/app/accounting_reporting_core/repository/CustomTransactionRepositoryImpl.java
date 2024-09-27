package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReconcilationCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterStatusRequest;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {

    private final EntityManager em;

    @Override
    public List<TransactionEntity> findAllByStatus(String organisationId,
                                                   List<ValidationStatus> validationStatuses,
                                                   List<TransactionType> transactionTypes) {
        // TODO what about order by entry date or transaction internal number, etc?
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TransactionEntity> criteriaQuery = builder.createQuery(TransactionEntity.class);

        Root<TransactionEntity> rootEntry = criteriaQuery.from(TransactionEntity.class);
        Predicate pValidationStatuses = builder.isTrue(builder.literal(true));
        Predicate pOrganisationId = builder.equal(rootEntry.get("organisation").get("id"), organisationId);
        Predicate pTransactionType = builder.isTrue(builder.literal(true));

        if (!validationStatuses.isEmpty()) {
            pValidationStatuses = builder.in(rootEntry.get("automatedValidationStatus")).value(validationStatuses);
        }

        criteriaQuery.select(rootEntry);
        criteriaQuery.where(pValidationStatuses, pOrganisationId, pTransactionType);

        return em.createQuery(criteriaQuery).getResultList();
    }


    @Override
    public List<TransactionEntity> findAllReconciliation(ReconciliationFilterStatusRequest filter) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TransactionEntity> criteriaQuery = builder.createQuery(TransactionEntity.class);
        Root<TransactionEntity> rootEntry = criteriaQuery.from(TransactionEntity.class);

        criteriaQuery.select(rootEntry);
        switch (filter) {
            case ReconciliationFilterStatusRequest.RENCONCILED -> {
                criteriaQuery.where(builder.equal(rootEntry.get("reconcilation").get("source"), ReconcilationCode.OK));
            }
            case ReconciliationFilterStatusRequest.UNRENCONCILED -> {
                criteriaQuery.where(builder.equal(rootEntry.get("reconcilation").get("source"), ReconcilationCode.NOK));
            }
            case ReconciliationFilterStatusRequest.UNPROCESSED -> {
                criteriaQuery.where(builder.isNull(rootEntry.get("reconcilation").get("source")));
            }
        }

        return em.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public Object[] findCalcReconciliationStatistic() {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<Object[]> mainQuery = builder.createQuery(Object[].class);

        Subquery<Long> countQueryNOK = mainQuery.subquery(Long.class);
        Root<TransactionEntity> rootEntryNOK = countQueryNOK.from(TransactionEntity.class);
        countQueryNOK.select(builder.count(rootEntryNOK));
        countQueryNOK.where(builder.equal(rootEntryNOK.get("reconcilation").get("source"), ReconcilationCode.NOK));


        Subquery<Long> countQueryOK = mainQuery.subquery(Long.class);
        Root<TransactionEntity> rootEntryOK = countQueryOK.from(TransactionEntity.class);
        countQueryOK.select(builder.count(rootEntryOK));
        countQueryOK.where(builder.equal(rootEntryOK.get("reconcilation").get("source"), ReconcilationCode.OK));

        Subquery<Long> countQueryNull = mainQuery.subquery(Long.class);
        Root<TransactionEntity> rootEntryNull = countQueryNull.from(TransactionEntity.class);
        countQueryNull.select(builder.count(rootEntryNull));
        countQueryNull.where(builder.isNull(rootEntryNull.get("reconcilation").get("source")));

        mainQuery.multiselect(countQueryOK.getSelection().alias("OK"), countQueryNOK.getSelection().alias("NOK"), countQueryNull.getSelection().alias("NOTYET"));

        return em.createQuery(mainQuery).getSingleResult();

    }

}
