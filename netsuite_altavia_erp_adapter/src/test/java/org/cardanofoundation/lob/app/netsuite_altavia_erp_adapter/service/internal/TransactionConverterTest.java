package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Validator;

import io.vavr.control.Either;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FatalError;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FinancialPeriodSource;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.Transactions;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.TxLine;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.CodeMappingType;

@ExtendWith(MockitoExtension.class)
class TransactionConverterTest {

    @Mock
    private Validator validator;
    @Mock
    private CodesMappingService codesMappingService;
    @Mock
    private PreprocessorService preprocessorService;
    @Mock
    private TransactionTypeMapper transactionTypeMapper;
    @Mock
    private FinancialPeriodSource financialPeriodSource;
    private final String netsuiteInstanceId = "netsuiteInstanceId";

    private TransactionConverter transactionConverter;

    @BeforeEach
    void setUp() {
        transactionConverter = new TransactionConverter(validator, codesMappingService, preprocessorService, transactionTypeMapper, netsuiteInstanceId, financialPeriodSource);
    }

    @Test
    void testConvert_OrganisationIdFailed() {
        TxLine mockTxLine = mock(TxLine.class);
        when(codesMappingService.getCodeMapping(netsuiteInstanceId, 10L, CodeMappingType.ORGANISATION)).thenReturn(Optional.empty());
        when(mockTxLine.subsidiary()).thenReturn(10L);

        Either<FatalError, Transactions> org = transactionConverter.convert("org", "10", List.of(mockTxLine));

        assertThat(org.isLeft()).isTrue();
    }




}
