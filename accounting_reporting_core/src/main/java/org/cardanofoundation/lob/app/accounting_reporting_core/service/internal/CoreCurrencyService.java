package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreCurrencyService {

    private final CoreCurrencyRepository coreCurrencyRepository;

    public List<CoreCurrency> listAll() {
        return coreCurrencyRepository.allCurrencies();
    }

    public Optional<CoreCurrency> findByCurrencyId(String currencyId) {
        return coreCurrencyRepository.findByCurrencyId(currencyId);
    }

}
