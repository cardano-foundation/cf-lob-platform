package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ProcessorFlags {

    @Builder.Default
    private boolean reprocess = false;

    public static ProcessorFlags defaultFlags() {
        return ProcessorFlags.builder().build();
    }

}
