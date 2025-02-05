package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionItemExtractionRepository {

    private final EntityManager em;

    public List<TransactionItemEntity> findByItemAccount(LocalDate dateFrom, LocalDate dateTo, List<String> accountCode, List<String> costCenter, List<String> project) {
        String jpql = "SELECT ti FROM accounting_reporting_core.TransactionItemEntity ti INNER JOIN ti.transaction te ";
        String where = "WHERE te.entryDate >= :dateFrom AND te.entryDate <= :dateTo ";

        if (null != accountCode && 0 < accountCode.stream().count()) {
            where += STR."AND (ti.accountDebit.code in (\{accountCode.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","))}) or ti.accountCredit.code in (\{accountCode.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","))})) AND ti.amountFcy <> 0 ";
        }

        if (null != costCenter && 0 < costCenter.stream().count()) {
            where += STR."AND ti.costCenter.externalCustomerCode in (\{costCenter.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","))}) ";
        }

        if (null != project && 0 < project.stream().count()) {
            where += STR."AND ti.project.customerCode in (\{project.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","))}) ";
        }

        where += STR."AND te.ledgerDispatchStatus = '\{LedgerDispatchStatus.FINALIZED}' ";

        Query resultQuery = em.createQuery(jpql + where);

        resultQuery.setParameter("dateFrom", dateFrom);
        resultQuery.setParameter("dateTo", dateTo);

        return resultQuery.getResultList();
    }
}
