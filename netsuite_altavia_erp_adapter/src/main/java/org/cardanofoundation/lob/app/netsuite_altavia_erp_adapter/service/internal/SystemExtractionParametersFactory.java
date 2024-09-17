package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingPeriodCalculator;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.zalando.problem.Problem;

@Slf4j
@RequiredArgsConstructor
public class SystemExtractionParametersFactory {

    private final OrganisationPublicApiIF organisationPublicApi;
    private final AccountingPeriodCalculator accountingPeriodCalculator;

    public Either<Problem, SystemExtractionParameters> createSystemExtractionParameters(String organisationId) {
        val organisationM = organisationPublicApi.findByOrganisationId(organisationId);

        if (organisationM.isEmpty()) {
            log.error("Organisation not found for id: {}", organisationId);

            val issue = Problem.builder()
                    .withTitle("ORGANISATION_NOT_FOUND")
                    .withDetail(STR."Organisation not found for id: \{organisationId}")
                    .build();

            return Either.left(issue);
        }
        val org = organisationM.orElseThrow();

        val period = accountingPeriodCalculator.calculateAccountingPeriod(org);

        return Either.right(SystemExtractionParameters.builder()
                .organisationId(organisationId)
                .accountPeriodFrom(period.getMinimum())
                .accountPeriodTo(period.getMaximum())
                .build()
        );
    }

}
