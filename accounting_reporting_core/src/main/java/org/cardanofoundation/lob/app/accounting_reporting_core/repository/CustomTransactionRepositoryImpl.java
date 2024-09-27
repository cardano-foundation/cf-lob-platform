package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

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
    public List<TransactionEntity> findAllReconciliation(){
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TransactionEntity> criteriaQuery = builder.createQuery(TransactionEntity.class);
        Root<TransactionEntity> rootEntry = criteriaQuery.from(TransactionEntity.class);

        criteriaQuery.select(rootEntry);

        return em.createQuery(criteriaQuery).getResultList();
    }

}
