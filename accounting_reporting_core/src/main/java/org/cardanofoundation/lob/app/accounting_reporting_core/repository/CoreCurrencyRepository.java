package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency;

import java.util.List;
import java.util.Optional;

public interface CoreCurrencyRepository {

    List<CoreCurrency> allCurrencies();

    Optional<CoreCurrency> findByCurrencyId(String currencyId);

}
