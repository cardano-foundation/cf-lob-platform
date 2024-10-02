package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Source;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

class DefaultBusinessRulesPipelineProcessorTest {

    private List<PipelineTask> pipelineTasks;
    private DefaultBusinessRulesPipelineProcessor processor;

    @BeforeEach
    void setUp() {
        pipelineTasks = List.of(mock(PipelineTask.class), mock(PipelineTask.class));
        processor = new DefaultBusinessRulesPipelineProcessor(pipelineTasks);
    }

    @Test
    void run_shouldValidateAndClearViolationsForAllTransactions() {
        // Arrange
        TransactionEntity transaction1 = mock(TransactionEntity.class);
        TransactionEntity transaction2 = mock(TransactionEntity.class);
        OrganisationTransactions allOrgTransactions = mock(OrganisationTransactions.class);

        when(allOrgTransactions.transactions()).thenReturn(Set.of(transaction1, transaction2));

        // Act
        processor.run(allOrgTransactions);

        // Assert
        verify(transaction1).clearAllViolations();
        verify(transaction2).clearAllViolations();

    }

    @Test
    void run_shouldExecuteAllPipelineTasks() {
        // Arrange
        TransactionEntity transaction = mock(TransactionEntity.class);
        OrganisationTransactions allOrgTransactions = mock(OrganisationTransactions.class);

        when(allOrgTransactions.transactions()).thenReturn(Set.of(transaction));

        // Act
        processor.run(allOrgTransactions);

        // Assert
        for (PipelineTask pipelineTask : pipelineTasks) {
            verify(pipelineTask).run(allOrgTransactions);
        }

    }

    @Test
    void run_shouldNotThrowExceptionWhenNoTransactions() {
        // Arrange
        OrganisationTransactions allOrgTransactions = mock(OrganisationTransactions.class);

        when(allOrgTransactions.transactions()).thenReturn(Set.of());

        // Act & Assert
        assertThatCode(() -> processor.run(allOrgTransactions))
                .doesNotThrowAnyException();
    }

    @Test
    void run_shouldValidateAndClearViolationsaNDiTEMSForAllTransactions() {
        // Arrange
        TransactionEntity transaction1 = mock(TransactionEntity.class);
        TransactionEntity transaction2 = mock(TransactionEntity.class);
        TransactionItemEntity transactionItemEntity1 = mock(TransactionItemEntity.class);
        transactionItemEntity1.setTransaction(transaction1);
        transaction1.setItems(Set.of(transactionItemEntity1));
        OrganisationTransactions allOrgTransactions = mock(OrganisationTransactions.class);

        when(allOrgTransactions.transactions()).thenReturn(Set.of(transaction1, transaction2));

        // Act
        processor.run(allOrgTransactions);

        // Assert
        verify(transaction1).clearAllViolations();

        verify(transaction2).clearAllViolations();
    }

}
