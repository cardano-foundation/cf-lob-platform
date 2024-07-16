package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

public interface PipelineTaskItem {

    void run(TransactionEntity transaction);

}
