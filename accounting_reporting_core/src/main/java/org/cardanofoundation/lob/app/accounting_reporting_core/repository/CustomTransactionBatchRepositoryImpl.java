package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import io.hypersistence.utils.hibernate.query.SQLExtractor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.OK;

@RequiredArgsConstructor
@Slf4j
public class CustomTransactionBatchRepositoryImpl implements CustomTransactionBatchRepository {
    private final EntityManager em;

    @Override
    public List<TransactionBatchEntity> findByFilter(BatchSearchRequest body) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TransactionBatchEntity> criteriaQuery = builder.createQuery(TransactionBatchEntity.class);
        Root<TransactionBatchEntity> rootEntry = criteriaQuery.from(TransactionBatchEntity.class);

        Collection<Predicate> andPredicates = queryCriteria(rootEntry, builder, body);

        criteriaQuery.select(rootEntry);
        criteriaQuery.where(andPredicates.toArray(new Predicate[0]));
        criteriaQuery.orderBy(builder.desc(rootEntry.get("createdAt")));
        // Without this line the query only returns one row.
        criteriaQuery.groupBy(rootEntry.get("id"));

        TypedQuery<TransactionBatchEntity> theQuery = em.createQuery(criteriaQuery);

        theQuery.setMaxResults(body.getLimit());

        if (null != body.getPage() && 0 < body.getPage()) {
            theQuery.setFirstResult(body.getPage() * body.getLimit());
        }

        return theQuery.getResultList();

    }

    @Override
    public Long findByFilterCount(BatchSearchRequest body) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        Root<TransactionBatchEntity> rootEntry = criteriaQuery.from(TransactionBatchEntity.class);
        Collection<Predicate> andPredicates = queryCriteria(rootEntry, builder, body);


        criteriaQuery.select(builder.count(rootEntry));
        criteriaQuery.where(andPredicates.toArray(new Predicate[0]));
        criteriaQuery.orderBy(builder.desc(rootEntry.get("createdAt")));
        // Without this line the query only returns one row.
        criteriaQuery.groupBy(rootEntry.get("id"));

        TypedQuery<Long> theQuery = em.createQuery(criteriaQuery);


        return theQuery.getResultList().stream().count();

    }

    private Collection<Predicate> queryCriteria(Root<TransactionBatchEntity> rootEntry, CriteriaBuilder builder, BatchSearchRequest body) {
        List<Predicate> andPredicates = new ArrayList<>();

        andPredicates.add(builder.equal(rootEntry.get("filteringParameters").get("organisationId"), body.getOrganisationId()));

        if (!body.getBatchStatistics().isEmpty()) {
            Join<TransactionBatchEntity, TransactionEntity> transactionEntityJoin = rootEntry.join("transactions", JoinType.INNER);

            List<Predicate> orPredicates = new ArrayList<>();

            if (0 < body.getBatchStatistics().stream().filter(s -> s.equals(LedgerDispatchStatusView.APPROVE)).count()) {
                orPredicates.add(builder.equal(transactionEntityJoin.get("ledgerDispatchStatus"), LedgerDispatchStatus.MARK_DISPATCH));
            }

            if (0 < body.getBatchStatistics().stream().filter(s -> s.equals(LedgerDispatchStatusView.PENDING)).count()) {
                orPredicates.add(builder.equal(transactionEntityJoin.get("ledgerDispatchStatus"), LedgerDispatchStatus.NOT_DISPATCHED));
            }

            if (0 < body.getBatchStatistics().stream().filter(s -> s.equals(LedgerDispatchStatusView.INVALID)).count()) {
                orPredicates.add(builder.equal(transactionEntityJoin.get("automatedValidationStatus"), ValidationStatus.FAILED));
            }

            if (0 < body.getBatchStatistics().stream().filter(s -> s.equals(LedgerDispatchStatusView.PUBLISH)).count()) {
                orPredicates.add(builder.equal(transactionEntityJoin.get("ledgerDispatchStatus"), LedgerDispatchStatus.DISPATCHED));
            }

            if (0 < body.getBatchStatistics().stream().filter(s -> s.equals(LedgerDispatchStatusView.PUBLISHED)).count()) {
                orPredicates.add(builder.equal(transactionEntityJoin.get("ledgerDispatchStatus"), LedgerDispatchStatus.COMPLETED));
            }

            andPredicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
        }

        if (!body.getTransactionTypes().isEmpty()) {
            Expression<?> bitwiseAnd = builder.function("BITAND", Integer.class, rootEntry.get("filteringParameters").get("transactionTypes"), builder.literal(body.getTransactionTypes().stream().toList()));
            andPredicates.add(builder.notEqual(bitwiseAnd, 0));
        }

        if (null != body.getFrom()) {
            LocalDateTime localDateTime1 = body.getFrom().atStartOfDay();
            andPredicates.add(builder.greaterThanOrEqualTo(rootEntry.get("createdAt"), localDateTime1));

        }

        if (null != body.getTo()) {
            LocalDateTime localDateTime2 = body.getTo().atTime(23,59, 59);
            andPredicates.add(builder.lessThanOrEqualTo(rootEntry.get("createdAt"), localDateTime2));
        }

        if (!body.getTxStatus().isEmpty()) {
            Join<TransactionBatchEntity, TransactionEntity> transactionEntityJoin = rootEntry.join("transactions", JoinType.INNER);
            andPredicates.add(builder.in(transactionEntityJoin.get("status")).value(body.getTxStatus()));
        }

        return andPredicates;
    }

}
