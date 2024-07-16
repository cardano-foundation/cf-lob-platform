package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service;

import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FinancialPeriodSource;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.Transactions;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.TxLine;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.CodeMappingType;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreBigDecimal;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreString;
import org.cardanofoundation.lob.app.support.collections.Optionals;

import java.time.YearMonth;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type.VENDOR;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FatalError.Code.ADAPTER_ERROR;
import static org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FieldType.*;
import static org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreString.normaliseString;

@RequiredArgsConstructor
@Slf4j
public class TransactionConverter {

    private final Validator validator;
    private final CodesMappingService codesMappingService;
    private final PreprocessorService preprocessorService;
    private final TransactionTypeMapper transactionTypeMapper;
    private final String netsuiteInstanceId;
    private final FinancialPeriodSource financialPeriodSource;

    public Either<FatalError, Transactions> convert(String organisationId,
                                                    String batchId,
                                                    List<TxLine> txLines) {
        val searchResultsByOrganisation = new ArrayList<TxLine>();
        val transactions = new LinkedHashSet<Transaction>();

        for (val txLine : txLines) {
            val localOrgIdE = organisationId(txLine);

            if (localOrgIdE.isEmpty()) {
                return Either.left(localOrgIdE.getLeft());
            }

            val localOrgId = localOrgIdE.get();

            if (localOrgId.equals(organisationId)) {
                searchResultsByOrganisation.add(txLine);
            }
        }

        val searchResultItemsPerTransactionNumber = searchResultsByOrganisation
                .stream()
                .collect(groupingBy(TxLine::transactionNumber));

        for (val entry : searchResultItemsPerTransactionNumber.entrySet()) {
            val transactionLevelTxLines = entry.getValue();
            val transactionE = createTransactionFromSearchResultItems(organisationId, batchId, transactionLevelTxLines);

            if (transactionE.isEmpty()) {
                return Either.left(transactionE.getLeft());
            }

            transactionE.get().ifPresent(transactions::add);
        }

        return Either.right(new Transactions(organisationId, transactions));
    }

    private Either<FatalError, Optional<Transaction>> createTransactionFromSearchResultItems(String organisationId,
                                                                                             String batchId,
                                                                                             List<TxLine> txLines
    ) {
        if (txLines.isEmpty()) {
            return Either.right(Optional.empty());
        }

        val firstTxLine = txLines.getFirst();
        val txId = Transaction.id(organisationId, firstTxLine.transactionNumber());

        val transTypeE = transactionType(organisationId, txId, firstTxLine);
        if (transTypeE.isEmpty()) {
            return Either.left(transTypeE.getLeft());
        }
        val transactionType = transTypeE.get();

        val txDate = firstTxLine.date();
        val internalTransactionNumber = firstTxLine.transactionNumber();
        val fxRate = firstTxLine.exchangeRate();
        val accountingPeriod = financialPeriod(firstTxLine);

        val txItems = new LinkedHashSet<TransactionItem>();

        for (val txLine : txLines) {
            val validationIssues = validator.validate(txLine);
            val isValid = validationIssues.isEmpty();

            if (!isValid) {
                val bag = Map.<String, Object>of(
                        "organisationId", organisationId,
                        "txId", txId,
                        "internalTransactionNumber", txLine.transactionNumber()
                );

                log.error("Validation failed for transaction: {}", bag);

                return Either.left(new FatalError(ADAPTER_ERROR, "TRANSACTIONS_VALIDATION_ERROR", bag));
            }

            val accountCreditCodeM = accountCreditCode(organisationId, txLine.accountMain());

            val amountLcy = MoreBigDecimal.substractNullFriendly(txLine.amountDebit(), txLine.amountCredit());
            val amountFcy = MoreBigDecimal.substractNullFriendly(txLine.amountDebitForeignCurrency(), txLine.amountCreditForeignCurrency());

            val costCenterM = costCenterCode(organisationId, txLine);
            val projectCodeM = projectCode(organisationId, txLine);

            val documentM = convertDocument(organisationId, txLine);

            val txItem = TransactionItem.builder()
                    .id(TransactionItem.id(txId, txLine.lineID().toString()))
                    .accountDebit(Optionals.zip(normaliseString(txLine.name()), normaliseString(txLine.number()), (accountDebitName, accountDebitCode) -> {
                        return Account.builder()
                                .name(Optional.of(accountDebitName))
                                .code(accountDebitCode)
                                .build();
                    }))
                    .accountCredit(accountCreditCodeM.map(accountCreditCode -> {
                                return Account.builder()
                                        .code(accountCreditCode)
                                        .build();
                            })
                    )
                    .project(projectCodeM.map(pc -> Project.builder()
                            .customerCode(pc)
                            .build()
                    ))
                    .costCenter(costCenterM.map(cc -> CostCenter.builder()
                            .customerCode(cc)
                            .build()
                    ))
                    .document(documentM)
                    .fxRate(fxRate)
                    .amountLcy(amountLcy)
                    .amountFcy(amountFcy)

                    .build();

            txItems.add(txItem);
        }

        return Either.right(Optional.of(Transaction.builder()
                .id(txId)
                .internalTransactionNumber(internalTransactionNumber)
                .entryDate(txDate)
                .batchId(batchId)
                .transactionType(transactionType)
                .accountingPeriod(accountingPeriod)
                .organisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation.builder()
                        .id(organisationId)
                        .build()
                )
                .items(txItems)
                .build())
        );
    }

    private YearMonth financialPeriod(TxLine txLine) {
        return switch (financialPeriodSource) {
            case IMPLICIT -> YearMonth.from(txLine.date());
            case EXPLICIT -> {
                val endDate = txLine.endDate();

                yield YearMonth.from(endDate);
            }
        };
    }

    private Either<FatalError, TransactionType> transactionType(String organisationId,
                                                                String txId,
                                                                TxLine txLine) {
        val transactionTypeM = transactionTypeMapper.apply(txLine.type());

        if (transactionTypeM.isEmpty()) {
            val bag = Map.<String, Object>of(
                    "organisationId", organisationId,
                    "internalTransactionNumber", txLine.transactionNumber(),
                    "txId", txId,
                    "type", txLine.type()
            );

            return Either.left(new FatalError(ADAPTER_ERROR, "TRANSACTION_TYPE_NOT_YET_KNOWN", bag));
        }

        return Either.right(transactionTypeM.orElseThrow());
    }

    private Optional<Document> convertDocument(String organisationId, TxLine txLine) {
        val documentNumberM = normaliseString(txLine.documentNumber());

        if (documentNumberM.isPresent()) {
            val documentNumber = documentNumberM.orElseThrow();

            val taxItemM = normaliseString(txLine.taxItem())
                    .map(String::trim);

            var vatCodeM = Optional.<String>empty();
            if (taxItemM.isPresent()) {
                val vatCodeE = preprocessorService.preProcess(taxItemM.orElseThrow(), VAT);

                if (vatCodeE.isEmpty()) {
                    log.warn("Conversion failed for vatCode: {} in organisation: {}", taxItemM.orElseThrow(), organisationId);

                    vatCodeM = Optional.of(taxItemM.orElseThrow());
                } else {
                    vatCodeM = Optional.of(vatCodeE.get());
                }
            }

            return Optional.of(Document.builder()
                    .number(documentNumber)
                    .currency(Currency.builder()
                            .customerCode(txLine.currencySymbol())
                            .build())
                    .vat(vatCodeM.map(cc -> Vat.builder()
                            .customerCode(cc)
                            .build()))
                    .counterparty(convertCounterparty(txLine))
                    .build()
            );
        }

        return Optional.empty();
    }

    private static Optional<Counterparty> convertCounterparty(TxLine txLine) {
        return normaliseString(txLine.id()).map(customerCode -> Counterparty.builder()
                .customerCode(customerCode)
                .type(VENDOR) // TODO CF hardcoded for now
                .name(normaliseString(txLine.companyName()))
                .build());
    }

    private Optional<String> accountCreditCode(String organisationId,
                                               String accountMain) {
        val accountCodeCreditM = normaliseString(accountMain);

        if (accountCodeCreditM.isPresent()) {
            val accountCodeCreditText = accountCodeCreditM.orElseThrow();

            val accountCreditCodeE = preprocessorService.preProcess(accountCodeCreditM.orElseThrow(), CHART_OF_ACCOUNT);

            if (accountCreditCodeE.isEmpty()) {
                log.warn("Conversion failed for accountCodeCredit: {} in organisation: {}", accountCodeCreditText, organisationId);

                return Optional.of(accountCodeCreditText);
            }

            val accountCreditCode = accountCreditCodeE.get();

            return Optional.of(accountCreditCode);
        }

        return Optional.empty();
    }

    private Optional<String> costCenterCode(String organisationId,
                                            TxLine txLine) {
        val costCenterM = MoreString.normaliseString(txLine.costCenter());

        if (costCenterM.isPresent()) {
            val costCenterText = costCenterM.orElseThrow();

            val costCenterE = preprocessorService.preProcess(costCenterText, COST_CENTER);

            if (costCenterE.isEmpty()) {
                log.warn("Conversion failed for costCenter: {} in organisation: {}", costCenterText, organisationId);

                return Optional.of(costCenterText);
            }
            val costCenter = costCenterE.get();

            return Optional.of(costCenter);
        }

        return Optional.empty();
    }

    private Either<FatalError, String> organisationId(TxLine txLine) {
        val organisationIdM = codesMappingService.getCodeMapping(netsuiteInstanceId, txLine.subsidiary(), CodeMappingType.ORGANISATION);

        if (organisationIdM.isEmpty()) {
            val bag = Map.<String, Object>of(
                    "netsuiteInstanceId", netsuiteInstanceId,
                    "subsidiary", txLine.subsidiary()
            );

            return Either.left(new FatalError(ADAPTER_ERROR, "ORGANISATION_NOT_IMPORTED", bag));
        }

        return Either.right(organisationIdM.orElseThrow());
    }

    private Optional<String> projectCode(String organisationId,
                                         TxLine txLine) {
        val projectM = normaliseString(txLine.project());

        if (projectM.isPresent()) {
            val projectText = projectM.orElseThrow();

            val projectCodeE = preprocessorService.preProcess(projectM.orElseThrow(), PROJECT);

            if (projectCodeE.isEmpty()) {
                log.warn("Conversion failed for projectCode: {} in organisation: {}", projectText, organisationId);

                return Optional.of(projectText);
            }

            val projectCode = projectCodeE.get();

            return Optional.of(projectCode);
        }

        return Optional.empty();
    }

}
