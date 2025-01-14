package org.cardanofoundation.lob.app.accounting_reporting_core.service.assistance;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.Range;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountingPeriodCalculator {

    private final Clock clock;

    public Range<LocalDate> calculateAccountingPeriod(Organisation organisation) {
        val toDate = LocalDate.now(clock).minusDays(1); // we exclude today
        val fromDate = toDate.minusDays(organisation.getAccountPeriodDays());

        return Range.of(fromDate, toDate);
    }

}
