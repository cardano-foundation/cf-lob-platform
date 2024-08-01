package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;

import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ViolationCode.PROJECT_DATA_NOT_FOUND;

@RequiredArgsConstructor
@Slf4j
public class ProjectConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApiIF organisationPublicApi;

    @Override
    public void run(TransactionEntity tx) {
        for (val txItem : tx.getItems()) {
            val projectM = txItem.getProject();

            if (projectM.isEmpty()) {
                continue;
            }

            val project = projectM.orElseThrow();

            val organisationId = tx.getOrganisation().getId();
            val customerCode = projectM.orElseThrow().getCustomerCode();

            log.info("Looking for project mapping for organisationId:{}, customerCode:{}", organisationId, customerCode);

            val projectMappingM = organisationPublicApi.findProject(organisationId, customerCode);

            log.info("Project mapping found: {}", projectMappingM);

            if (projectMappingM.isEmpty()) {
                val v = Violation.builder()
                        .code(PROJECT_DATA_NOT_FOUND)
                        .txItemId(txItem.getId())
                        .severity(ERROR)
                        .source(LOB)
                        .processorModule(this.getClass().getSimpleName())
                        .bag(
                                Map.of(
                                        "transactionNumber", tx.getTransactionInternalNumber()
                                )
                        )
                        .build();

                tx.addViolation(v);

                continue;
            }

            val projectMapping = projectMappingM.orElseThrow();

            val p = project.toBuilder()
                    .customerCode(projectMapping.getId().getCustomerCode())
                    .externalCustomerCode(projectMapping.getExternalCustomerCode())
                    .name(projectMapping.getName())
                    .build();

            txItem.setProject(p);
        }
    }

}