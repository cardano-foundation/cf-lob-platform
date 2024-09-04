package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.zalando.problem.Problem;

import java.time.Clock;
import java.time.YearMonth;

@Slf4j
@RequiredArgsConstructor
public class SystemExtractionParametersFactory {

    private final Clock clock;
    private final OrganisationPublicApiIF organisationPublicApi;

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
        val organisation = organisationM.orElseThrow();

        val currentAccountingPeriod = YearMonth.now(clock);

        return Either.right(SystemExtractionParameters.builder()
                .organisationId(organisationId)
                .accountPeriodFrom(currentAccountingPeriod.minusMonths(organisation.getAccountPeriodMonths()))
                .accountPeriodTo(currentAccountingPeriod)
                .build()
        );
    }

}
