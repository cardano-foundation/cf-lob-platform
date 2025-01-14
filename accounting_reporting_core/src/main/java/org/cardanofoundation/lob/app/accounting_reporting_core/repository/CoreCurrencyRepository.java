package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import java.util.List;
import java.util.Optional;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency;

public interface CoreCurrencyRepository {

    List<CoreCurrency> allCurrencies();

    Optional<CoreCurrency> findByCurrencyId(String currencyId);

}
