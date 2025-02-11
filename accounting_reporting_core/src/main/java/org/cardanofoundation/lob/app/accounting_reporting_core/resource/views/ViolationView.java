package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionViolationCode;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

@Getter
@Setter
@AllArgsConstructor
public class ViolationView {
    private Violation.Severity severity;
    private Source source;
    private Optional<String> transactionItemId;
    private TransactionViolationCode code;
    private Map<String, Object> bag;

}
