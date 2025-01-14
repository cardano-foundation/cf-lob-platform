package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.PipelineTaskItem;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Setter
public class DefaultPipelineTask implements PipelineTask {

    private final List<PipelineTaskItem> items;

    @Override
    public void run(OrganisationTransactions orgTransactions) {
        for (val transactionEntity : orgTransactions.transactions()) {
            runTaskItems(transactionEntity);
        }
    }

    private void runTaskItems(TransactionEntity transaction) {
        for (val taskItem : items) {
            taskItem.run(transaction);
        }
    }

}
