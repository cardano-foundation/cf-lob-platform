package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.*;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.VendorBill;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;
import static org.junit.jupiter.api.Assertions.*;

class CustomBlockchainReaderAccountingCoreTransactionRepositoryImplTestView {


    @Test
    void findAllByStatus() {
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<TransactionEntity> criteriaQuery = Mockito.mock(String.valueOf(TransactionEntity.class));
        EntityManager em = Mockito.mock(EntityManager.class);

        TransactionEntity transactionEntity = Mockito.mock(TransactionEntity.class);
        TransactionEntity transactionEntity2 = Mockito.mock(TransactionEntity.class);
        transactionEntity.setId("nothing");
        transactionEntity2.setId("nothing2");
        List<TransactionEntity> transactions = List.of(transactionEntity, transactionEntity2);
        Root<TransactionEntity> rootEntry = Mockito.mock(String.valueOf(TransactionEntity.class));

        TypedQuery<TransactionEntity> transactionEntityTypedQuery = Mockito.mock(String.valueOf(TransactionEntity.class));


        Mockito.when(em.getCriteriaBuilder()).thenReturn(builder);
        Mockito.when(builder.createQuery(TransactionEntity.class)).thenReturn(criteriaQuery);
        Mockito.when(criteriaQuery.from(TransactionEntity.class)).thenReturn(rootEntry);
        Mockito.when(transactionEntityTypedQuery.getResultList()).thenReturn(transactions);
        Path<Object> validationStatus = Mockito.mock(Path.class);
        Path<Object> organisation = Mockito.mock(Path.class);
        Path<Object> organisationId = Mockito.mock(Path.class);

        CriteriaBuilder.In inResult = Mockito.mock(CriteriaBuilder.In.class);

        Mockito.when(rootEntry.get("automatedValidationStatus")).thenReturn(validationStatus);
        Mockito.when(rootEntry.get("organisation")).thenReturn(organisation);
        Mockito.when(organisation.get("id")).thenReturn(organisationId);
        Mockito.when(builder.in(rootEntry.get("automatedValidationStatus"))).thenReturn(inResult);

        Mockito.when(em.createQuery(criteriaQuery)).thenReturn(transactionEntityTypedQuery);
        CustomTransactionRepositoryImpl customTransactionRepository = new CustomTransactionRepositoryImpl(em);

        List<TransactionEntity> elresult = customTransactionRepository.findAllByStatus("OrgId", List.of(ValidationStatus.valueOf(String.valueOf(VALIDATED))), List.of(TransactionType.valueOf(String.valueOf(VendorBill))));

        //Mockito.verify(builder,Mockito.times(4)).isTrue(builder.literal(true));
        Mockito.verify(builder, Mockito.times(1)).in(validationStatus);
        Mockito.verify(builder, Mockito.times(1)).equal(organisationId, "OrgId");
        Mockito.verify(inResult, Mockito.times(1)).value(List.of(ValidationStatus.valueOf(String.valueOf(VALIDATED))));
        Mockito.verify(rootEntry, Mockito.times(1)).get("organisation");
        Mockito.verify(organisation, Mockito.times(1)).get("id");
        Assertions.assertEquals(List.of(transactionEntity, transactionEntity2), elresult);
    }
}