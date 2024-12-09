package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.IntervalType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.report.ReportType;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCurrency;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class ReportingParametersView {

    Set<ReportType> reportType;

    Set<IntervalType> intervalTypes;

    Set<String> organisationCurrency;
}
