package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;

import java.util.List;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@RequiredArgsConstructor
@Slf4j
public class DefaultBusinessRulesPipelineProcessor implements BusinessRulesPipelineProcessor {

    private final List<PipelineTask> pipelineTasks;

    @Override
    public void run(final OrganisationTransactions allOrgTransactions, ProcessorFlags processorFlags) {
        val trigger = processorFlags.getTrigger();

        for (val transactionEntity : allOrgTransactions.transactions()) {
            transactionEntity.setAutomatedValidationStatus(VALIDATED);
            transactionEntity.clearAllViolations();

            switch (trigger) {
                // Remove all transaction items rejection with a ERP source, this happens only when a batch is created
                case EXTRACTION -> {
                    transactionEntity.clearAllItemsRejectionsSource(Source.ERP);
                }
                // Remove all transaction items rejection with a LOB source, this happens only when a batch in reprocessed
                case REPROCESSING -> {
                    transactionEntity.clearAllItemsRejectionsSource(Source.LOB);
                }
            }
        }

        for (val pipelineTask : pipelineTasks) {
            pipelineTask.run(allOrgTransactions);
        }
    }

}
