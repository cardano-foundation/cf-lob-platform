package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProcessorFlags {

    private Trigger trigger;

    public enum Trigger {

        EXTRACTION, // extraction of data from ERP source
        RECONCILATION, // reconciliation of extracted data
        REPROCESSING // reprocessing some failed transactions within a batch

    }

}
