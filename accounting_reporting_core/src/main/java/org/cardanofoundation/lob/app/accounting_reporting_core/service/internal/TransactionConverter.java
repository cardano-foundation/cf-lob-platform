package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Account;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.AccountEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.CostCenter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Counterparty;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Document;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Project;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Vat;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service("accounting_reporting_core.TransactionConverter")
@Slf4j
@RequiredArgsConstructor
public class TransactionConverter {

    private final CoreCurrencyService coreCurrencyService;
    private final Clock clock;

    public FilteringParameters convertToDbDetached(SystemExtractionParameters systemExtractionParameters,
                                                   UserExtractionParameters userExtractionParameters) {
        return FilteringParameters.builder()
                .organisationId(userExtractionParameters.getOrganisationId())
                .transactionTypes(userExtractionParameters.getTransactionTypes())
                .from(userExtractionParameters.getFrom())
                .to(userExtractionParameters.getTo())
                .accountingPeriodFrom(systemExtractionParameters.getAccountPeriodFrom())
                .accountingPeriodTo(systemExtractionParameters.getAccountPeriodTo())
                .transactionNumbers(userExtractionParameters.getTransactionNumbers())
                .build();
    }

    public FilteringParameters convertToDbDetached(UserExtractionParameters userExtractionParameters,
                                                   Optional<SystemExtractionParameters> systemExtractionParameters) {
        return systemExtractionParameters.map(se -> {
            return convertToDbDetached(se, userExtractionParameters);
        }).orElseGet(() -> convertToDbDetached(userExtractionParameters));
    }

    public FilteringParameters convertToDbDetached(UserExtractionParameters userExtractionParameters) {
        return FilteringParameters.builder()
                .organisationId(userExtractionParameters.getOrganisationId())
                .transactionTypes(userExtractionParameters.getTransactionTypes())
                .from(userExtractionParameters.getFrom())
                .to(userExtractionParameters.getTo())
                .transactionNumbers(userExtractionParameters.getTransactionNumbers())
                .build();
    }

    public Set<TransactionEntity> convertToDbDetached(Set<Transaction> transactions) {
        return transactions.stream()
                .map(this::convertToDbDetached)
                .collect(Collectors.toSet());
    }

    public Set<Transaction> convertFromDb(Set<TransactionEntity> transactionEntities) {
        return transactionEntities.stream()
                .map(this::convertToDbDetached)
                .collect(Collectors.toSet());
    }

    private TransactionEntity convertToDbDetached(Transaction transaction) {
        val violations = transaction.getViolations()
                .stream()
                .map(violation -> {
                    val violationEntity = new Violation();
                    violationEntity.setCode(violation.code());
                    violationEntity.setTxItemId(violation.txItemId());
                    violationEntity.setSeverity(violation.severity());
                    violationEntity.setSource(violation.source());
                    violationEntity.setProcessorModule(violation.processorModule());
                    violationEntity.setBag(violation.bag());

                    return violationEntity;
                })
                .collect(Collectors.toSet());

        val txItems = transaction.getItems()
                .stream()
                .map(txItem -> {
                    val doc = convertToDbDetached(txItem.getDocument());

                    val txItemEntity = new TransactionItemEntity();

                    txItemEntity.setId(txItem.getId());
                    txItemEntity.setDocument(doc);
                    txItemEntity.setAmountLcy(txItem.getAmountLcy());
                    txItemEntity.setAmountFcy(txItem.getAmountFcy());
                    txItemEntity.setCostCenter(convertCostCenter(txItem.getCostCenter()));
                    txItemEntity.setProject(convertProject(txItem.getProject()));
                    txItemEntity.setFxRate(txItem.getFxRate());
                    txItem.getAccountCredit().ifPresent(creditAccount -> {
                        txItemEntity.setAccountCredit(Account.builder()
                                .code(creditAccount.getCode())
                                .refCode(creditAccount.getRefCode().orElse(null))
                                .name(creditAccount.getName().orElse(null))
                                .build());
                    });

                    txItem.getAccountDebit().ifPresent(debitAccount -> {
                        txItemEntity.setAccountDebit(Account.builder()
                                .code(debitAccount.getCode())
                                .refCode(debitAccount.getRefCode().orElse(null))
                                .name(debitAccount.getName().orElse(null))
                                .build());
                    });

                    txItem.getAccountEvent().ifPresent(accountEvent -> {
                        txItemEntity.setAccountEvent(AccountEvent.builder()
                                .code(accountEvent.getCode())
                                .name(accountEvent.getName())
                                .build());
                    });

                    return txItemEntity;
                })
                .collect(Collectors.toSet());

        val txEntity = new TransactionEntity();
        txEntity.setId(transaction.getId());
        txEntity.setBatchId(transaction.getBatchId());
        txEntity.setTransactionInternalNumber(transaction.getInternalTransactionNumber());
        txEntity.setTransactionType(transaction.getTransactionType());
        txEntity.setEntryDate(transaction.getEntryDate());
        txEntity.setOrganisation(convertOrganisation(transaction));
        txEntity.setAutomatedValidationStatus(transaction.getValidationStatus());
        txEntity.setLedgerDispatchStatus(transaction.getLedgerDispatchStatus());
        txEntity.setAccountingPeriod(transaction.getAccountingPeriod());
        txEntity.setTransactionApproved(transaction.isTransactionApproved());
        txEntity.setLedgerDispatchApproved(transaction.isLedgerDispatchApproved());
        txEntity.setUpdatedAt(LocalDateTime.now(clock));

        txItems.forEach(i -> i.setTransaction(txEntity));

        txEntity.setViolations(violations);
        txEntity.setItems(txItems);

        return txEntity;
    }

    private Project convertProject(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Project> projectM) {
        return projectM.map(p -> Project.builder()
                        .customerCode(p.getCustomerCode())
                        .externalCustomerCode(p.getExternalCustomerCode().orElse(null))
                        .name(p.getName().orElse(null))
                        .build())
                .orElse(null);
    }

    private CostCenter convertCostCenter(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter> costCenter) {
        return costCenter.map(cc -> CostCenter.builder()
                        .customerCode(cc.getCustomerCode())
                        .externalCustomerCode(cc.getExternalCustomerCode().orElse(null))
                        .name(cc.getName().orElse(null))
                        .build())
                .orElse(null);
    }

    private static Organisation convertOrganisation(Transaction transaction) {
        return Organisation.builder()
                .id(transaction.getOrganisation().getId())
                .name(transaction.getOrganisation().getName().orElse(null))
                .taxIdNumber(transaction.getOrganisation().getTaxIdNumber().orElse(null))
                .countryCode(transaction.getOrganisation().getCountryCode().orElse(null))
                .currencyId(transaction.getOrganisation().getCurrencyId())
                .adminEmail(transaction.getOrganisation().getAdminEmail().orElse(null))
                .build();
    }

    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Document convertToDbDetached(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document> docM) {
        return docM.map(doc -> Document.builder()
                        .num(doc.getNumber())

                        .currency(Currency.builder()
                                .customerCode(doc.getCurrency().getCustomerCode())
                                .id(doc.getCurrency().getCoreCurrency().map(CoreCurrency::toExternalId).orElse(null))
                                .build())

                        .vat(doc.getVat().map(vat -> Vat.builder()
                                .customerCode(vat.getCustomerCode())
                                .rate(vat.getRate().orElse(null))
                                .build()).orElse(null))

                        .counterparty(doc.getCounterparty().map(counterparty -> Counterparty.builder()
                                .customerCode(counterparty.getCustomerCode())
                                .type(counterparty.getType())
                                .name(counterparty.getName().orElse(null))
                                .build()).orElse(null)))

                .map(Document.DocumentBuilder::build)
                .orElse(null);
    }

    private Transaction convertToDbDetached(TransactionEntity transactionEntity) {
        val violations = transactionEntity.getViolations()
                .stream()
                .map(violationEntity -> {
                    return new org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation(
                            violationEntity.getSeverity(),
                            violationEntity.getSource(),
                            violationEntity.getTxItemId(),
                            violationEntity.getCode(),
                            violationEntity.getProcessorModule(),
                            violationEntity.getBag()
                    );
                })
                .collect(Collectors.toSet());

        val items = transactionEntity.getItems()
                .stream()
                .map(txItemEntity -> TransactionItem.builder()
                        .id(txItemEntity.getId())

                        .accountDebit(txItemEntity.getAccountDebit().map(account -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Account.builder()
                                .code(account.getCode())
                                .refCode(account.getRefCode())
                                .name(account.getName())
                                .build()))

                        .accountCredit(txItemEntity.getAccountCredit().map(account -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Account.builder()
                                .code(account.getCode())
                                .refCode(account.getRefCode())
                                .name(account.getName())
                                .build()))

                        .accountEvent(txItemEntity.getAccountEvent().map(accountEvent -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.AccountEvent.builder()
                                .code(accountEvent.getCode())
                                .name(accountEvent.getName())
                                .build()))

                        .project(txItemEntity.getProject().map(project -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Project.builder()
                                .customerCode(project.getCustomerCode())
                                .externalCustomerCode(project.getExternalCustomerCode())
                                .name(project.getName())
                                .build()))

                        .costCenter(txItemEntity.getCostCenter().map(costCenter -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter.builder()
                                .customerCode(costCenter.getCustomerCode())
                                .externalCustomerCode(costCenter.getExternalCustomerCode())
                                .name(costCenter.getName())
                                .build()))

                        .document(txItemEntity.getDocument()
                                .flatMap(this::convertToDbDetached))

                        .fxRate(txItemEntity.getFxRate())

                        .amountFcy(txItemEntity.getAmountFcy())
                        .amountLcy(txItemEntity.getAmountLcy())

                        .build())
                .collect(Collectors.toSet());

        return Transaction.builder()
                .id(transactionEntity.getId())
                .batchId(transactionEntity.getBatchId())
                .organisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation.builder()
                        .id(transactionEntity.getOrganisation().getId())
                        .name(transactionEntity.getOrganisation().getName())
                        .countryCode(transactionEntity.getOrganisation().getCountryCode())
                        .taxIdNumber(transactionEntity.getOrganisation().getTaxIdNumber())
                        .adminEmail(transactionEntity.getOrganisation().getAdminEmail())
                        .currencyId(transactionEntity.getOrganisation().getCurrencyId())
                        .build())

                .entryDate(transactionEntity.getEntryDate())
                .validationStatus(transactionEntity.getAutomatedValidationStatus())
                .transactionType(transactionEntity.getTransactionType())
                .internalTransactionNumber(transactionEntity.getTransactionInternalNumber())

                .transactionApproved(transactionEntity.getTransactionApproved())
                .ledgerDispatchStatus(transactionEntity.getLedgerDispatchStatus())
                .ledgerDispatchApproved(transactionEntity.getLedgerDispatchApproved())
                .accountingPeriod(transactionEntity.getAccountingPeriod())
                .items(items)
                .violations(violations)
                .build();
    }

    private Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document> convertToDbDetached(@Nullable Document doc) {
        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document.builder()
                .number(doc.getNum())
                .currency(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                        .customerCode(doc.getCurrency().getCustomerCode())
                        .coreCurrency(doc.getCurrency().getId().flatMap(coreCurrencyService::findByCurrencyId))
                        .build()
                )
                .vat(doc.getVat().map(vat -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Vat.builder()
                        .customerCode(vat.getCustomerCode())
                        .rate(vat.getRate())
                        .build()))

                .counterparty(doc.getCounterparty().map(counterparty -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.builder()
                        .customerCode(counterparty.getCustomerCode())
                        .name(counterparty.getName())
                        .build()))

                .build());
    }

    public void copyFields(TransactionEntity attached,
                           TransactionEntity detached) {
        attached.setId(detached.getId());
        attached.setBatchId(detached.getBatchId());
        attached.setOrganisation(detached.getOrganisation());
        attached.setLedgerDispatchApproved(detached.getLedgerDispatchApproved());
        attached.setTransactionApproved(detached.getTransactionApproved());
        attached.setTransactionType(detached.getTransactionType());
        attached.setEntryDate(detached.getEntryDate());
        attached.setAutomatedValidationStatus(detached.getAutomatedValidationStatus());
        attached.setLedgerDispatchStatus(detached.getLedgerDispatchStatus());
        attached.setAccountingPeriod(detached.getAccountingPeriod());
        attached.setTransactionInternalNumber(detached.getTransactionInternalNumber());
        attached.setUpdatedAt(LocalDateTime.now(clock));
        attached.setUpdatedBy(detached.getUpdatedBy());

        attached.getViolations().clear();
        attached.getViolations().addAll(detached.getViolations());

        attached.getItems().clear();
        attached.getItems().addAll(detached.getItems());
    }

}
