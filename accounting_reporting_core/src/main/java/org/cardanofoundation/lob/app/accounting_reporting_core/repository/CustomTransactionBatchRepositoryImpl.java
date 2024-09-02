package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.RejectionCode.*;

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
        Collection<Predicate> andPredicates = new ArrayList<>();

        andPredicates.add(builder.equal(rootEntry.get("filteringParameters").get("organisationId"), body.getOrganisationId()));

        if (!body.getBatchStatistics().isEmpty()) {
            Join<TransactionBatchEntity, TransactionEntity> transactionEntityJoin = rootEntry.join("transactions", JoinType.INNER);

            List<Predicate> orPredicates = new ArrayList<>();

            if (body.getBatchStatistics().stream().anyMatch(s -> s.equals(LedgerDispatchStatusView.INVALID))) {

                orPredicates.add(transactionEntityJoin.get("items").get("rejection").get("rejectionCode").as(Integer.class).in(RejectionCode.getSourceBasedRejectionCodes(Source.ERP).stream().map(Enum::ordinal).toList()));
                Subquery<String> subqueryErp = builder.createQuery().subquery(String.class);
                Root<TransactionEntity> transactionEntityRoot = subqueryErp.from(TransactionEntity.class);
                subqueryErp.select(transactionEntityRoot.get("id"));
                Predicate whereErp = builder.equal(transactionEntityRoot.get("violations").get("source"), "ERP");
                subqueryErp.where(whereErp);
                orPredicates.add((builder.in(transactionEntityJoin.get("id")).value(subqueryErp)));

            }

            if (body.getBatchStatistics().stream().anyMatch(s -> s.equals(LedgerDispatchStatusView.PENDING))) {

                List<Predicate> andPredicatesJoin = new ArrayList<>();

                Subquery<String> subqueryItemsIn = builder.createQuery(rootEntry.getClass()).subquery(String.class);
                Root<TransactionEntity> transactionEntityRootItem = subqueryItemsIn.from(TransactionEntity.class);

                subqueryItemsIn.select(transactionEntityRootItem.get("id"));
                Predicate whereItem =
                        transactionEntityJoin.get("items").get("rejection").get("rejectionCode").in(RejectionCode.getSourceBasedRejectionCodes(Source.LOB).stream().map(Enum::ordinal).toList());
                subqueryItemsIn.where(whereItem);

                Subquery<String> subqueryItemsOut = builder.createQuery(rootEntry.getClass()).subquery(String.class);
                Root<TransactionEntity> transactionEntityRootItem2 = subqueryItemsOut.from(TransactionEntity.class);


                subqueryItemsOut.select(transactionEntityRootItem2.get("id"));
                Predicate whereItem2 =
                        transactionEntityJoin.get("items").get("rejection").get("rejectionCode").in(RejectionCode.getSourceBasedRejectionCodes(Source.ERP).stream().map(Enum::ordinal).toList());
                subqueryItemsOut.where(whereItem2);

                Subquery<String> subqueryErp = builder.createQuery().subquery(String.class);
                Root<TransactionEntity> transactionEntityRoot = subqueryErp.from(TransactionEntity.class);
                subqueryErp.select(transactionEntityRoot.get("id"));
                Predicate whereErp = builder.equal(transactionEntityRoot.get("violations").get("source"), "ERP");
                subqueryErp.where(whereErp);

                Subquery<String> subqueryLob = builder.createQuery().subquery(String.class);
                Root<TransactionEntity> transactionEntityRootLob = subqueryLob.from(TransactionEntity.class);
                subqueryLob.select(transactionEntityRootLob.get("id"));
                Predicate whereLob = builder.equal(transactionEntityRootLob.get("violations").get("source"), "LOB");
                subqueryLob.where(whereLob);

                andPredicatesJoin.add(builder.not(builder.in(transactionEntityJoin.get("id")).value(subqueryErp)));
                andPredicatesJoin.add((builder.in(transactionEntityJoin.get("id")).value(subqueryLob)));

                orPredicates.add(builder.and(builder.in(transactionEntityJoin.get("id")).value(subqueryItemsOut).not(), builder.in(transactionEntityJoin.get("id")).value(subqueryItemsIn)));

                orPredicates.add(builder.and(andPredicatesJoin.toArray(new Predicate[0])));
            }

            if (body.getBatchStatistics().stream().anyMatch(s -> s.equals(LedgerDispatchStatusView.APPROVE))) {
                orPredicates.add(builder.and(builder.equal(transactionEntityJoin.get("transactionApproved"), false), builder.equal(transactionEntityJoin.get("ledgerDispatchApproved"), false), builder.equal(transactionEntityJoin.get("automatedValidationStatus"), ValidationStatus.VALIDATED)));

            }

            if (body.getBatchStatistics().stream().anyMatch(s -> s.equals(LedgerDispatchStatusView.PUBLISH))) {
                orPredicates.add(builder.and(builder.equal(transactionEntityJoin.get("transactionApproved"), true), builder.equal(transactionEntityJoin.get("ledgerDispatchApproved"), false), builder.equal(transactionEntityJoin.get("automatedValidationStatus"), ValidationStatus.VALIDATED)));

            }

            if (body.getBatchStatistics().stream().anyMatch(s -> s.equals(LedgerDispatchStatusView.PUBLISHED))) {
                orPredicates.add(builder.and(builder.equal(transactionEntityJoin.get("transactionApproved"), true), builder.equal(transactionEntityJoin.get("ledgerDispatchApproved"), true), builder.equal(transactionEntityJoin.get("automatedValidationStatus"), ValidationStatus.VALIDATED)));

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
            LocalDateTime localDateTime2 = body.getTo().atTime(23, 59, 59);
            andPredicates.add(builder.lessThanOrEqualTo(rootEntry.get("createdAt"), localDateTime2));
        }

        if (!body.getTxStatus().isEmpty()) {
            Join<TransactionBatchEntity, TransactionEntity> transactionEntityJoin = rootEntry.join("transactions", JoinType.INNER);
            andPredicates.add(builder.in(transactionEntityJoin.get("status")).value(body.getTxStatus()));
        }

        return andPredicates;
    }

}
