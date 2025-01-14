package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;

import static org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags.Trigger.REPROCESSING;

@RequiredArgsConstructor
public class BusinessRulesPipelineSelector implements BusinessRulesPipelineProcessor {

    private final BusinessRulesPipelineProcessor defaultBusinessRulesProcessor;
    private final BusinessRulesPipelineProcessor reprocessBusinessRulesProcessor;

    @Override
    public void run(OrganisationTransactions passedTransactions, ProcessorFlags processorFlags) {
        if (processorFlags.getTrigger() == REPROCESSING) {
            reprocessBusinessRulesProcessor.run(passedTransactions, processorFlags);
            return;
        }

        defaultBusinessRulesProcessor.run(passedTransactions, processorFlags);
    }

}
