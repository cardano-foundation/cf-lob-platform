package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

@ExtendWith(MockitoExtension.class)
class TransactionConverterTest {

    @Mock
    private CoreCurrencyService coreCurrencyService;
    @Mock
    OrganisationConverter organisationConverter;

    @InjectMocks
    private TransactionConverter transactionConverter;

    private UserExtractionParameters userParams;
    private SystemExtractionParameters systemParams;
    private Transaction transaction;
    private TransactionEntity transactionEntity;

    @BeforeEach
    void setUp() {
        userParams = mock(UserExtractionParameters.class);
        systemParams = mock(SystemExtractionParameters.class);
        transaction = mock(Transaction.class);
        transactionEntity = mock(TransactionEntity.class);
    }

    @Test
    void testConvertToDbDetached_WithSystemAndUserParams() {
        when(userParams.getOrganisationId()).thenReturn("org123");
        FilteringParameters result = transactionConverter.convertToDbDetached(systemParams, userParams);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("org123", result.getOrganisationId());
    }

    @Test
    void testConvertToDbDetached_WithOptionalSystemParams() {
        when(userParams.getOrganisationId()).thenReturn("org123");
        Optional<SystemExtractionParameters> optionalParams = Optional.of(systemParams);
        FilteringParameters result = transactionConverter.convertToDbDetached(userParams, optionalParams);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("org123", result.getOrganisationId());
    }

    @Test
    void testConvertToDbDetached_UserParamsOnly() {
        when(userParams.getOrganisationId()).thenReturn("org123");
        FilteringParameters result = transactionConverter.convertToDbDetached(userParams);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("org123", result.getOrganisationId());
    }

    @Test
    void testConvertToDbDetached_SetOfTransactions() {
        Set<Transaction> transactions = Set.of(transaction);
        Set<TransactionEntity> result = transactionConverter.convertToDbDetached(transactions);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void testConvertFromDb_SetOfTransactionEntities() {
        Set<TransactionEntity> transactionEntities = Set.of(transactionEntity);
        Set<Transaction> result = transactionConverter.convertFromDb(transactionEntities);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void testCopyFields() {
        TransactionEntity attached = new TransactionEntity();
        TransactionEntity detached = new TransactionEntity();
        detached.setTransactionType(TransactionType.BillCredit);

        transactionConverter.copyFields(attached, detached);
        Assertions.assertEquals(TransactionType.BillCredit, attached.getTransactionType());
    }
}
