package org.cardanofoundation.lob.app.accounting_reporting_core.config;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.DefaultBusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.DefaultPipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BusinessRulesConfig {

    private final Validator validator;
    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository currencyRepository;

    @Bean
    public BusinessRulesPipelineProcessor businessRulesPipelineProcessor() {
        val pipelineTasks = new ArrayList<PipelineTask>();

        pipelineTasks.add(sanityCheckPipelineTask());
        pipelineTasks.add(preCleansingPipelineTask());
        pipelineTasks.add(preValidationPipelineTask());
        pipelineTasks.add(conversionPipelineTask());
        pipelineTasks.add(postCleansingPipelineTask());
        pipelineTasks.add(postValidationPipelineTask());
        pipelineTasks.add(sanityCheckPipelineTask());

        return new DefaultBusinessRulesPipelineProcessor(pipelineTasks);
    }

    private PipelineTask sanityCheckPipelineTask() {
        return new DefaultPipelineTask(List.of(
                new SanityCheckFieldsTaskItem(validator)
        ));
    }

    private PipelineTask preCleansingPipelineTask() {
        return new DefaultPipelineTask(List.of(
                new DiscardZeroBalanceTxItemsTaskItem(),
                new JournalAccountCreditEnrichmentTaskItem(organisationPublicApi),
                new JournalAccountAmountsEnrichmentTaskItem() // this must be after JournalAccountCreditEnrichmentTaskItem
        ));
    }

    private PipelineTask preValidationPipelineTask() {
        return new DefaultPipelineTask(List.of(
                new AmountsFcyCheckTaskItem(),
                new AmountsLcyCheckTaskItem(),
                new AmountLcyBalanceZerosOutCheckTaskItem(),
                new AmountFcyBalanceZerosOutCheckTaskItem()
        ));
    }

    private PipelineTask conversionPipelineTask() {
        return new DefaultPipelineTask(List.of(
                new OrganisationConversionTaskItem(organisationPublicApi, currencyRepository),
                new DocumentConversionTaskItem(organisationPublicApi, currencyRepository),
                new CostCenterConversionTaskItem(organisationPublicApi),
                new ProjectConversionTaskItem(organisationPublicApi),
                new AccountEventCodesConversionTaskItem(organisationPublicApi)
        ));
    }

    private PipelineTask postCleansingPipelineTask() {
        return new DefaultPipelineTask(List.of(
                new DebitAccountCheckTaskItem(),
                new TxItemsCollapsingTaskItem()
        ));
    }

    private PipelineTask postValidationPipelineTask() {
        return new DefaultPipelineTask(List.of(
                new AccountCodeDebitCheckTaskItem(),
                new AccountCodeCreditCheckTaskItem(),
                new DocumentMustBePresentTaskItem(),
                new NoTransactionItemsTaskItem()
        ));
    }

}