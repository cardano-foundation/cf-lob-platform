package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxValidationStatus.VALIDATED;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;

@RequiredArgsConstructor
@Slf4j
public class DefaultBusinessRulesPipelineProcessor implements BusinessRulesPipelineProcessor {

    private final List<PipelineTask> pipelineTasks;

    @Override
    public void run(final OrganisationTransactions allOrgTransactions, ProcessorFlags processorFlags) {

        for (val transactionEntity : allOrgTransactions.transactions()) {
            transactionEntity.setAutomatedValidationStatus(VALIDATED);
            if (processorFlags.getTrigger() == ProcessorFlags.Trigger.IMPORT) {
                transactionEntity.clearAllViolations();
            }
            if (processorFlags.getTrigger() == ProcessorFlags.Trigger.REPROCESSING) {
                transactionEntity.clearAllViolations(Source.LOB);
            }

        }

        for (val pipelineTask : pipelineTasks) {
            pipelineTask.run(allOrgTransactions);
        }
    }

}
