package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import lombok.Getter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;

import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;

@Getter
public enum RejectionReason {

    INCORRECT_AMOUNT(ERP),
    INCORRECT_COST_CENTER(ERP),
    INCORRECT_PROJECT(ERP),
    INCORRECT_CURRENCY(ERP),
    INCORRECT_VAT_CODE(ERP),
    REVIEW_PARENT_COST_CENTER(LOB),
    REVIEW_PARENT_PROJECT_CODE(LOB);

    private final Source source;

    RejectionReason(Source source) {
        this.source = source;
    }

    public static Set<RejectionReason> getSourceBasedRejectionReasons(Source source) {
        return Set.of(RejectionReason.values())
                .stream()
                .filter(rejectionReason -> rejectionReason.getSource() == source)
                .collect(Collectors.toSet());
    }
}
