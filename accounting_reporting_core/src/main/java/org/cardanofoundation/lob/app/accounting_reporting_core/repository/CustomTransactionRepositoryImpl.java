package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import io.hypersistence.utils.hibernate.query.SQLExtractor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReconcilationCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ReconciliationFilterStatusRequest;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<Object[]> findAllReconciliationSpecial(Set<ReconcilationRejectionCode> rejectionCodes, Integer limit, Integer page) {

        String jpql = reconciliationQuery(rejectionCodes);

        Query reconciliationQuery = em.createQuery(jpql);

        reconciliationQuery.setMaxResults(limit);

        if (null != page && 0 < page) {
            reconciliationQuery.setFirstResult(page * limit);
        }
        return reconciliationQuery.getResultList();

    }

    @Override
    public List<Object[]> findAllReconciliationSpecialCount(Set<ReconcilationRejectionCode> rejectionCodes, Integer limit, Integer page) {

        String jpql = "SELECT count(rv.transactionId) " +
                "FROM accounting_reporting_core.ReconcilationEntity r " +
                "JOIN r.violations rv " +
                "LEFT JOIN accounting_reporting_core.TransactionEntity tr ON rv.transactionId = tr.id " +
                "where (r.id = tr.lastReconcilation.id or tr.id is null) ";

        String where = "";
        if (!rejectionCodes.isEmpty()) {
            where += " and rv.rejectionCode in (" + rejectionCodes.stream().map(Objects::toString).collect(Collectors.joining(",")) + ") ";
        }
        where += "group by rv.transactionId,tr.id,rv.amountLcySum,rv.rejectionCode,rv.sourceDiff,rv.transactionEntryDate,rv.transactionInternalNumber,rv.transactionType ";


        return em.createQuery(jpql + where).getResultList();

    }

    @Override
    public List<TransactionEntity> findAllReconciliation(ReconciliationFilterStatusRequest filter, Integer limit, Integer page) {

        switch (filter) {
            case ReconciliationFilterStatusRequest.RENCONCILED -> {
                CriteriaBuilder builder = em.getCriteriaBuilder();
                CriteriaQuery<TransactionEntity> criteriaQuery = builder.createQuery(TransactionEntity.class);
                Root<TransactionEntity> rootEntry = criteriaQuery.from(TransactionEntity.class);

                criteriaQuery.select(rootEntry);
                criteriaQuery.where(builder.and(builder.equal(rootEntry.get("reconcilation").get("finalStatus"), ReconcilationCode.OK)));
                TypedQuery<TransactionEntity> theQuery = em.createQuery(criteriaQuery);
                theQuery.setMaxResults(limit);
                if (null != page && 0 < page) {
                    theQuery.setFirstResult(page * limit);
                }
                return theQuery.getResultList();
            }
            case ReconciliationFilterStatusRequest.UNPROCESSED -> {
                CriteriaBuilder builder = em.getCriteriaBuilder();
                CriteriaQuery<TransactionEntity> criteriaQuery = builder.createQuery(TransactionEntity.class);
                Root<TransactionEntity> rootEntry = criteriaQuery.from(TransactionEntity.class);

                criteriaQuery.select(rootEntry);
                criteriaQuery.where(builder.isNull(rootEntry.get("reconcilation").get("source")));
                TypedQuery<TransactionEntity> theQuery = em.createQuery(criteriaQuery);
                theQuery.setMaxResults(limit);
                if (null != page && 0 < page) {
                    theQuery.setFirstResult(page * limit);
                }
                return theQuery.getResultList();
            }
        }

        return List.of();
    }

    @Override
    public Object[] findCalcReconciliationStatistic() {

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<Object[]> mainQuery = builder.createQuery(Object[].class);

        Subquery<Long> countQueryNOK = mainQuery.subquery(Long.class);
        Root<TransactionEntity> rootEntryNOK = countQueryNOK.from(TransactionEntity.class);
        countQueryNOK.select(builder.count(rootEntryNOK));
        countQueryNOK.where(builder.equal(rootEntryNOK.get("reconcilation").get("finalStatus"), ReconcilationCode.NOK));


        Subquery<Long> countQueryOK = mainQuery.subquery(Long.class);
        Root<TransactionEntity> rootEntryOK = countQueryOK.from(TransactionEntity.class);
        countQueryOK.select(builder.count(rootEntryOK));
        countQueryOK.where(builder.equal(rootEntryOK.get("reconcilation").get("finalStatus"), ReconcilationCode.OK));

        Subquery<Long> countQueryNull = mainQuery.subquery(Long.class);
        Root<TransactionEntity> rootEntryNull = countQueryNull.from(TransactionEntity.class);
        countQueryNull.select(builder.count(rootEntryNull));
        countQueryNull.where(builder.isNull(rootEntryNull.get("reconcilation").get("source")));

        mainQuery.multiselect(countQueryOK.getSelection().alias("OK"), countQueryNOK.getSelection().alias("NOK"), countQueryNull.getSelection().alias("NOTYET"));

        return em.createQuery(mainQuery).getSingleResult();

    }

    private String reconciliationQuery(Set<ReconcilationRejectionCode> rejectionCodes) {
        
        String jpql = "SELECT tr, rv " +
                "FROM accounting_reporting_core.ReconcilationEntity r " +
                "JOIN r.violations rv " +
                "LEFT JOIN accounting_reporting_core.TransactionEntity tr ON rv.transactionId = tr.id " +
                "where (r.id = tr.lastReconcilation.id or tr.lastReconcilation.id is null) ";

        String where = "";
        if (!rejectionCodes.isEmpty()) {
            where += " and rv.rejectionCode in (" + rejectionCodes.stream().map(Objects::toString).collect(Collectors.joining(",")) + ") ";
        }

        where += "group by rv.transactionId,tr.id,rv.amountLcySum,rv.rejectionCode,rv.sourceDiff,rv.transactionEntryDate,rv.transactionInternalNumber,rv.transactionType ";
        return jpql + where;
    }
}
