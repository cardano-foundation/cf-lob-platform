package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.LedgerDispatchStatusView;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomTransactionBatchRepositoryImplTest {

    @Test
    void findByFilterTest() {
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        EntityManager em = Mockito.mock(EntityManager.class);
        CriteriaQuery<TransactionBatchEntity> criteriaQuery = Mockito.mock(String.valueOf(TransactionBatchEntity.class));
        Root<TransactionBatchEntity> rootEntry = Mockito.mock(String.valueOf(TransactionBatchEntity.class));
        TypedQuery<TransactionBatchEntity> theQuery = Mockito.mock(String.valueOf(TransactionBatchEntity.class));
        Path<Object> filteringParameters = Mockito.mock(Path.class);
        Path<Object> organisationId = Mockito.mock(Path.class);
        Join transactionEntityJoin = Mockito.mock(Join.class);
        CriteriaBuilder.In inResult = Mockito.mock(CriteriaBuilder.In.class);

        BatchSearchRequest body = new BatchSearchRequest();
        body.setLimit(10);
        body.setPage(1);
        body.setOrganisationId("TestOrgId");
        body.setBatchStatistics(Set.of(LedgerDispatchStatusView.APPROVE, LedgerDispatchStatusView.PENDING, LedgerDispatchStatusView.INVALID, LedgerDispatchStatusView.PUBLISH, LedgerDispatchStatusView.PUBLISHED));
        body.setTxStatus(Set.of(TransactionStatus.OK));
        body.setTransactionTypes(Set.of(TransactionType.CardCharge, TransactionType.FxRevaluation));
        body.setFrom(LocalDate.now());
        body.setTo(LocalDate.now());

        Mockito.when(em.getCriteriaBuilder()).thenReturn(builder);
        Mockito.when(builder.createQuery(TransactionBatchEntity.class)).thenReturn(criteriaQuery);
        Mockito.when(criteriaQuery.from(TransactionBatchEntity.class)).thenReturn(rootEntry);
        Mockito.when(rootEntry.get("filteringParameters")).thenReturn(filteringParameters);
        Mockito.when(filteringParameters.get("organizationId")).thenReturn(organisationId);
        Mockito.when(rootEntry.join("transactions", JoinType.INNER)).thenReturn(transactionEntityJoin);
        Mockito.when(builder.in(transactionEntityJoin.get("status"))).thenReturn(inResult);

        Mockito.when(em.createQuery(criteriaQuery)).thenReturn(theQuery);
        CustomTransactionBatchRepositoryImpl customTransactionBatchRepository = new CustomTransactionBatchRepositoryImpl(em);

        List<TransactionBatchEntity> result = customTransactionBatchRepository.findByFilter(body);

        Mockito.verify(builder,Mockito.times(1)).equal(transactionEntityJoin.get("ledgerDispatchStatus"), LedgerDispatchStatus.MARK_DISPATCH);
        Mockito.verify(builder,Mockito.times(1)).equal(transactionEntityJoin.get("ledgerDispatchStatus"), LedgerDispatchStatus.NOT_DISPATCHED);
        Mockito.verify(builder,Mockito.times(1)).equal(transactionEntityJoin.get("ledgerDispatchStatus"), LedgerDispatchStatus.DISPATCHED);
        Mockito.verify(builder,Mockito.times(1)).equal(transactionEntityJoin.get("ledgerDispatchStatus"), LedgerDispatchStatus.COMPLETED);
        Mockito.verify(builder,Mockito.times(1)).equal(transactionEntityJoin.get("automatedValidationStatus"), ValidationStatus.FAILED);
        Mockito.verify(theQuery,Mockito.times(1)).setFirstResult(10);

    }

    @Test
    void findByFilterAllTest() {
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        EntityManager em = Mockito.mock(EntityManager.class);
        CriteriaQuery<TransactionBatchEntity> criteriaQuery = Mockito.mock(String.valueOf(TransactionBatchEntity.class));
        Root<TransactionBatchEntity> rootEntry = Mockito.mock(String.valueOf(TransactionBatchEntity.class));
        TypedQuery<TransactionBatchEntity> theQuery = Mockito.mock(String.valueOf(TransactionBatchEntity.class));
        Path<Object> filteringParameters = Mockito.mock(Path.class);
        Path<Object> organisationId = Mockito.mock(Path.class);
        Join transactionEntityJoin = Mockito.mock(Join.class);
        CriteriaBuilder.In inResult = Mockito.mock(CriteriaBuilder.In.class);

        BatchSearchRequest body = new BatchSearchRequest();
        body.setLimit(10);
        body.setPage(1);
        body.setOrganisationId("TestOrgId");

        Mockito.when(em.getCriteriaBuilder()).thenReturn(builder);
        Mockito.when(builder.createQuery(TransactionBatchEntity.class)).thenReturn(criteriaQuery);
        Mockito.when(criteriaQuery.from(TransactionBatchEntity.class)).thenReturn(rootEntry);
        Mockito.when(rootEntry.get("filteringParameters")).thenReturn(filteringParameters);
        Mockito.when(filteringParameters.get("organizationId")).thenReturn(organisationId);
        Mockito.when(rootEntry.join("transactions", JoinType.INNER)).thenReturn(transactionEntityJoin);
        Mockito.when(builder.in(transactionEntityJoin.get("status"))).thenReturn(inResult);

        Mockito.when(em.createQuery(criteriaQuery)).thenReturn(theQuery);
        CustomTransactionBatchRepositoryImpl customTransactionBatchRepository = new CustomTransactionBatchRepositoryImpl(em);

        List<TransactionBatchEntity> result = customTransactionBatchRepository.findByFilter(body);

        Mockito.verify(transactionEntityJoin,Mockito.times(0)).get("ledgerDispatchStatus");
        Mockito.verify(transactionEntityJoin,Mockito.times(0)).get("automatedValidationStatus");
        Mockito.verify(theQuery,Mockito.times(1)).setFirstResult(10);

    }
}