package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;

public interface BusinessRulesPipelineProcessor {

    void run(OrganisationTransactions passedTransactions, ProcessorFlags processorFlags);

}
