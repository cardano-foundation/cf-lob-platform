package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PIIDataFilteringService implements Function<Set<Transaction>, Set<Transaction>> {

    @Override
    public Set<Transaction> apply(Set<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return transactions;
        }

        log.info("Filtering PII from passedTransactions, size:{}", transactions.size());

        return transactions.stream()
                .map(trx -> {
                    val txItems = trx.getItems().stream()
                            .map(txItem -> txItem.toBuilder()
                                    .accountCredit(Optional.empty())
                                    .accountDebit(Optional.empty())
                                    .document(convert(txItem.getDocument()))
                                    .build())
                            .collect(Collectors.toSet());

                    return trx.toBuilder()
                            .items(txItems)
                            .violations(Set.of())
                            .build();
                })
                .collect(Collectors.toSet());
    }

    private Optional<Document> convert(Optional<Document> documentM) {
        return documentM.map(doc -> doc.toBuilder()
                .counterparty(doc.getCounterparty()
                        .map(counterparty -> counterparty.toBuilder()
                                .name(Optional.empty())
                                .build()))
                .build()
        );
    }

}
