package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Slf4j
public class TxItemsCollapsingTaskItem implements PipelineTaskItem {
    @Override
    public void run(TransactionEntity tx) {
        if (tx.getAutomatedValidationStatus() == FAILED) {
            return;
        }

        val itemsPerKeyMap = tx.getItems()
                .stream()
                .collect(groupingBy(txItem -> TransactionItemKey.builder()
                        .costCenterCustomerCode(txItem.getCostCenter().map(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.CostCenter::getCustomerCode))
                        .documentVatCustomerCode(txItem.getDocument().flatMap(d -> d.getVat().map(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Vat::getCustomerCode)))
                        .documentNum(txItem.getDocument().map(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Document::getNum))
                        .documentCurrencyId(txItem.getDocument().flatMap(d -> d.getCurrency().getId()))
                        .documentCounterpartyCustomerCode(txItem.getDocument().flatMap(d -> d.getCounterparty().map(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Counterparty::getCustomerCode)))
                        .accountEventCode(txItem.getAccountEvent().map(org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.AccountEvent::getCode))
                        .build())
                );

        val txItems = itemsPerKeyMap.values().stream()
                .map(items -> items.stream()
                        .reduce((txItem1, txItem2) -> {

                            txItem1.setAmountFcy(txItem1.getAmountFcy().add(txItem2.getAmountFcy()));
                            txItem1.setAmountLcy(txItem1.getAmountLcy().add(txItem2.getAmountLcy()));

                            return txItem1;
                        })
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        tx.getItems().clear();
        tx.getItems().addAll(txItems);
    }

    @EqualsAndHashCode
    @Builder
    @Getter
    public static class TransactionItemKey {

        @Builder.Default
        private Optional<String> costCenterCustomerCode = Optional.empty();

        @Builder.Default
        private Optional<String> documentNum = Optional.empty();

        @Builder.Default
        private Optional<String> documentVatCustomerCode = Optional.empty();

        @Builder.Default
        private Optional<String> documentCounterpartyCustomerCode = Optional.empty();

        @Builder.Default
        private Optional<String> documentCurrencyId = Optional.empty();

        @Builder.Default
        private Optional<String> accountEventCode = Optional.empty();

    }

}
