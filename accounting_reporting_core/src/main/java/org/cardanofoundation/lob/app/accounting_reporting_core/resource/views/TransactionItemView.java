package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Account;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class TransactionItemView {
    private String id;
    private Optional<Account> accountDebit=  Optional.empty();
    private Optional<Account> accountCredit= Optional.empty();
    private BigDecimal amountFcy;
    private BigDecimal amountLcy;

}
