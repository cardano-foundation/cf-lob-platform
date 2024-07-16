package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;
import java.util.Optional;

public record Violation(Severity severity,
                        Source source,
                        Optional<String> txItemId,
                        ViolationCode code,
                        String processorModule,
                        Map<String, Object> bag) {

    public static Violation create(Severity severity,
                                   Source source,
                                   ViolationCode violationCode,
                                   String processorModule,
                                   Map<String, Object> bag) {
        return new Violation(severity, source, Optional.empty(), violationCode, processorModule, bag);
    }

    public static Violation create(Severity severity,
                                   Source source,
                                   String txItemId,
                                   ViolationCode violationCode,
                                   String processorModule,
                                   Map<String, Object> bag) {
        return new Violation(severity, source, Optional.of(txItemId), violationCode, processorModule, bag);
    }

    public enum Severity {
        WARN,
        ERROR
    }

}
