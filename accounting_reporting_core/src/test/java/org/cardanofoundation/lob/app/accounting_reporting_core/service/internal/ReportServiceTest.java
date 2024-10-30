package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import io.vavr.control.Either;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.ReportEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.ReportRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zalando.problem.Problem;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private OrganisationPublicApi organisationPublicApi;

    @InjectMocks
    private ReportService reportService;

    private static final String REPORT_ID = "test-report-id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void approveReport_whenReportExists_shouldSetReportApproved() {
        // Arrange
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setReportId(REPORT_ID);
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(reportEntity));

        // Act
        Either<Problem, Void> result = reportService.approveReport(REPORT_ID);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(reportEntity.getReportApproved()).isTrue();
        verify(reportRepository).save(reportEntity);
    }

    @Test
    void approveReport_whenReportDoesNotExist_shouldReturnProblem() {
        // Arrange
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        // Act
        Either<Problem, Void> result = reportService.approveReport(REPORT_ID);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getTitle()).isEqualTo("REPORT_NOT_FOUND");
        verify(reportRepository, never()).save(any());
    }

    @Test
    void approveReportForLedgerDispatch_whenReportExists_shouldSetLedgerDispatchApproved() {
        // Arrange
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setReportId(REPORT_ID);
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(reportEntity));

        // Act
        Either<Problem, Void> result = reportService.approveReportForLedgerDispatch(REPORT_ID);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(reportEntity.getLedgerDispatchApproved()).isTrue();
        verify(reportRepository).save(reportEntity);
    }

    @Test
    void approveReportForLedgerDispatch_whenReportDoesNotExist_shouldReturnProblem() {
        // Arrange
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        // Act
        Either<Problem, Void> result = reportService.approveReportForLedgerDispatch(REPORT_ID);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getTitle()).isEqualTo("REPORT_NOT_FOUND");
        verify(reportRepository, never()).save(any());
    }

    @Test
    void exists_whenReportExists_shouldReturnTrue() {
        // Arrange
        when(reportRepository.existsById(REPORT_ID)).thenReturn(true);

        // Act
        boolean result = reportService.exists(REPORT_ID);

        // Assert
        assertThat(result).isTrue();
        verify(reportRepository).existsById(REPORT_ID);
    }

    @Test
    void exists_whenReportDoesNotExist_shouldReturnFalse() {
        // Arrange
        when(reportRepository.existsById(REPORT_ID)).thenReturn(false);

        // Act
        boolean result = reportService.exists(REPORT_ID);

        // Assert
        assertThat(result).isFalse();
        verify(reportRepository).existsById(REPORT_ID);
    }

    @Test
    void findById_whenReportExists_shouldReturnReport() {
        // Arrange
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setReportId(REPORT_ID);
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(reportEntity));

        // Act
        Optional<ReportEntity> result = reportService.findById(REPORT_ID);

        // Assert
        assertThat(result).isPresent().contains(reportEntity);
        verify(reportRepository).findById(REPORT_ID);
    }

    @Test
    void findById_whenReportDoesNotExist_shouldReturnEmpty() {
        // Arrange
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        // Act
        Optional<ReportEntity> result = reportService.findById(REPORT_ID);

        // Assert
        assertThat(result).isNotPresent();
        verify(reportRepository).findById(REPORT_ID);
    }

    @Test
    void isReportValid_whenReportExistsAndIsValid_shouldReturnTrue() {
        // Arrange
        ReportEntity reportEntity = mock(ReportEntity.class);
        when(reportEntity.isValid()).thenReturn(true);
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(reportEntity));

        // Act
        Either<Problem, Boolean> result = reportService.isReportValid(REPORT_ID);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isTrue();
        verify(reportEntity).isValid();
    }

    @Test
    void isReportValid_whenReportExistsAndIsNotValid_shouldReturnFalse() {
        // Arrange
        ReportEntity reportEntity = mock(ReportEntity.class);
        when(reportEntity.isValid()).thenReturn(false);
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(reportEntity));

        // Act
        Either<Problem, Boolean> result = reportService.isReportValid(REPORT_ID);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isFalse();
        verify(reportEntity).isValid();
    }

    @Test
    void isReportValid_whenReportDoesNotExist_shouldReturnProblem() {
        // Arrange
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        // Act
        Either<Problem, Boolean> result = reportService.isReportValid(REPORT_ID);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getTitle()).isEqualTo("REPORT_NOT_FOUND");
        verify(reportRepository).findById(REPORT_ID);
    }

}