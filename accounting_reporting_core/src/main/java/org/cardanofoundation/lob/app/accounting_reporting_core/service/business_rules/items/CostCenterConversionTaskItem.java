package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;

import java.util.Map;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Severity.ERROR;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ViolationCode.COST_CENTER_DATA_NOT_FOUND;

@RequiredArgsConstructor
public class CostCenterConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApiIF organisationPublicApi;

    @Override
    public void run(TransactionEntity tx) {
        val organisationId = tx.getOrganisation().getId();

        for (val txItem : tx.getItems()) {
            val costCenterM = txItem.getCostCenter();

            if (costCenterM.isEmpty()) {
                return;
            }

            val costCenter = costCenterM.orElseThrow();
            val customerCode = costCenter.getCustomerCode();

            val costCenterMappingM = organisationPublicApi.findCostCenter(organisationId, customerCode);

            if (costCenterMappingM.isEmpty()) {
                val v = Violation.builder()
                        .code(COST_CENTER_DATA_NOT_FOUND)
                        .txItemId(txItem.getId())
                        .severity(ERROR)
                        .source(LOB)
                        .processorModule(this.getClass().getSimpleName())
                        .bag(
                                Map.of(
                                        "customerCode", customerCode,
                                        "transactionNumber", tx.getTransactionInternalNumber()
                                )
                        )
                        .build();

                tx.addViolation(v);

                return;
            }

            val costCenterMapping = costCenterMappingM.orElseThrow();

            txItem.setCostCenter(Optional.of(costCenter.toBuilder()
                    .customerCode(customerCode)
                    .externalCustomerCode(costCenterMapping.getExternalCustomerCode())
                    .name(costCenterMapping.getName())
                    .build())
            );
        }

    }

}
