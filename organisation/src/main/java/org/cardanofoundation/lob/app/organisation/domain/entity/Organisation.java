package org.cardanofoundation.lob.app.organisation.domain.entity;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLCITextType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Optional;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "organisation")
public class Organisation {

    @Id
    @Column(name = "organisation_id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tax_id_number", nullable = false)
    private String taxIdNumber;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "dummy_account")
    private String dummyAccount;

    @Column(name = "pre_approve_transactions")
    private Boolean preApproveTransactions;

    @Column(name = "pre_approve_transactions_dispatch")
    private Boolean preApproveTransactionsDispatch;

    @Column(name = "accounting_period_days", nullable = false)
    private int accountPeriodDays; // how many days in the past from yesterday

    @Column(name = "currency_id", nullable = false)
    private String currencyId;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Lob
    @Type(PostgreSQLCITextType.class)
    @Column(name = "logo")
    private String logo;

    public static String id(String countryCode, String taxIdNumber) {
        return digestAsHex(STR."\{countryCode}::\{taxIdNumber}");
    }

    public boolean isPreApproveTransactionsEnabled() {
        return Optional.ofNullable(preApproveTransactions).orElse(false);
    }

    public boolean isPreApproveTransactionsDispatchEnabled() {
        return Optional.ofNullable(preApproveTransactionsDispatch).orElse(false);
    }

    public Optional<String> getDummyAccount() {
        return Optional.ofNullable(dummyAccount);
    }

}
