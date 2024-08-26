import io.vavr.control.Either;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoFinalityScore;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.CardanoOnChainData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.ChainTip;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainPublishStatusMapper;
import org.cardanofoundation.lob.app.blockchain_publisher.service.CardanoFinalityProvider;
import org.cardanofoundation.lob.app.blockchain_publisher.service.TransactionsWatchDogService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain.BlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain.BlockchainTransactionDataProvider;
import org.cardanofoundation.lob.app.blockchain_publisher.service.on_chain.CardanoFinalityScoreCalculator;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.zalando.problem.Problem;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionsWatchDogServiceTest {

    @Mock
    private TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;

    @Mock
    private BlockchainTransactionDataProvider blockchainTransactionDataProvider;

    @Mock
    private BlockchainDataChainTipService blockchainDataChainTipService;

    @Mock
    private OrganisationPublicApiIF organisationPublicApiIF;

    @Mock
    private LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;

    @Mock
    private BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    @Mock
    private CardanoFinalityScoreCalculator cardanoFinalityScoreCalculator;

    @Mock
    private CardanoFinalityProvider cardanoFinalityProvider;

    @InjectMocks
    private TransactionsWatchDogService transactionsWatchDogService;


    @Test
    void testCheckTransactionStatusesForOrganisation_returnsNonEmptyList() {
        // Arrange
        val organisation1 = new Organisation();
        organisation1.setId("org1");

        when(organisationPublicApiIF.listAll()).thenReturn(List.of(organisation1));

        when(transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(eq("org1"), any(Limit.class)))
                .thenReturn(emptySet());

        when(blockchainDataChainTipService.latestChainTip()).thenReturn(Either.right(new ChainTip(1L, "hash1")));

        // Act
        List<Either<Problem, Set<TransactionEntity>>> result = transactionsWatchDogService.checkTransactionStatusesForOrganisation(Integer.MAX_VALUE);

        // Assert
        assertThat(result).isNotEmpty();
    }

    @Test
    void testInspectOrganisationTransactions_withEmptyChainTip() {
        // Arrange
        when(blockchainDataChainTipService.latestChainTip()).thenReturn(Either.left(Problem.builder()
                .withTitle("CHAIN_TIP_NOT_FOUND")
                .build()));

        // Act
        val result = transactionsWatchDogService.inspectOrganisationTransactions("org1", Integer.MAX_VALUE);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getTitle()).isEqualTo("CHAIN_TIP_NOT_FOUND");
    }

    @Test
    void testInspectOrganisationTransactions_withOnChainDataCompleted() {
        val chainTip = 1000L;
        val txAbsoluteSlotNow = chainTip - 50;
        val txAbsoluteSlotThen = chainTip - 500;

        when(cardanoFinalityScoreCalculator.calculateFinalityScore(chainTip, txAbsoluteSlotNow))
                .thenReturn(CardanoFinalityScore.VERY_HIGH);

        // Arrange
        val l1SubmissionData = new L1SubmissionData();
        l1SubmissionData.setTransactionHash("hash1");
        l1SubmissionData.setAbsoluteSlot(txAbsoluteSlotThen);
        l1SubmissionData.setPublishStatus(Optional.of(BlockchainPublishStatus.VISIBLE_ON_CHAIN));

        val transactionEntity = new TransactionEntity();
        transactionEntity.setL1SubmissionData(Optional.of(l1SubmissionData));

        when(transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(eq("org1"), any(Limit.class)))
                .thenReturn(Set.of(transactionEntity));

        when(blockchainDataChainTipService.latestChainTip()).thenReturn(Either.right(new ChainTip(chainTip, "hash2")));

        // return latest on-chain data
        when(blockchainTransactionDataProvider.getCardanoOnChainData(l1SubmissionData.getTransactionHash().orElseThrow()))
                .thenReturn(Either.right(Optional.of(new CardanoOnChainData("hash1", txAbsoluteSlotNow))));

        when(blockchainPublishStatusMapper.convert(any(CardanoFinalityScore.class))).thenReturn(BlockchainPublishStatus.COMPLETED);

        // Act
        val result = transactionsWatchDogService.inspectOrganisationTransactions("org1", Integer.MAX_VALUE);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).contains(transactionEntity);

        assertThat(transactionEntity.getL1SubmissionData().orElseThrow().getPublishStatus().orElseThrow())
                .isEqualTo(BlockchainPublishStatus.COMPLETED);
    }

    @Test
    void testInspectOrganisationTransactions_withOnChainDataFinalized() {
        val chainTip = 1000L;
        val txAbsoluteSlotNow = chainTip - 2160;
        val txAbsoluteSlotThen = chainTip - 999;

        when(cardanoFinalityScoreCalculator.calculateFinalityScore(chainTip, txAbsoluteSlotNow))
                .thenReturn(CardanoFinalityScore.FINAL);

        // Arrange
        val l1SubmissionData = new L1SubmissionData();
        l1SubmissionData.setTransactionHash("hash1");
        l1SubmissionData.setAbsoluteSlot(txAbsoluteSlotThen);
        l1SubmissionData.setPublishStatus(Optional.of(BlockchainPublishStatus.VISIBLE_ON_CHAIN));

        val transactionEntity = new TransactionEntity();
        transactionEntity.setL1SubmissionData(Optional.of(l1SubmissionData));

        when(transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(eq("org1"), any(Limit.class)))
                .thenReturn(Set.of(transactionEntity));

        when(blockchainDataChainTipService.latestChainTip()).thenReturn(Either.right(new ChainTip(chainTip, "hash2")));

        // return latest on-chain data
        when(blockchainTransactionDataProvider.getCardanoOnChainData(l1SubmissionData.getTransactionHash().orElseThrow()))
                .thenReturn(Either.right(Optional.of(new CardanoOnChainData("hash1", txAbsoluteSlotNow))));

        when(blockchainPublishStatusMapper.convert(any(CardanoFinalityScore.class))).thenReturn(BlockchainPublishStatus.FINALIZED);

        // Act
        val result = transactionsWatchDogService.inspectOrganisationTransactions("org1", Integer.MAX_VALUE);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).contains(transactionEntity);

        assertThat(transactionEntity.getL1SubmissionData().orElseThrow().getPublishStatus().orElseThrow())
                .isEqualTo(BlockchainPublishStatus.FINALIZED);
    }

    @Test
    void testInspectOrganisationTransactions_withRollbackScenario() {
        val chainTip = 100L;

        val txSubmissionData = new L1SubmissionData();
        txSubmissionData.setPublishStatus(Optional.of(BlockchainPublishStatus.VISIBLE_ON_CHAIN));
        txSubmissionData.setFinalityScore(Optional.of(CardanoFinalityScore.LOW));
        txSubmissionData.setTransactionHash("hash1");
        txSubmissionData.setAbsoluteSlot(chainTip - 80);

        // Arrange
        val transactionEntity = new TransactionEntity();
        transactionEntity.setId("tx1");
        transactionEntity.setL1SubmissionData(Optional.of(txSubmissionData));

        when(transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(eq("org1"), any(Limit.class)))
                .thenReturn(Set.of(transactionEntity));

        when(blockchainDataChainTipService.latestChainTip()).thenReturn(Either.right(new ChainTip(chainTip, "hash2")));
        when(blockchainTransactionDataProvider.getCardanoOnChainData(txSubmissionData.getTransactionHash().orElseThrow()))
                .thenReturn(Either.right(Optional.empty()));

        // Act
        val result = transactionsWatchDogService.inspectOrganisationTransactions("org1", Integer.MAX_VALUE);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).contains(transactionEntity);
        // Verify that the transaction status was updated to ROLLBACKED
        assertThat(transactionEntity.getL1SubmissionData().orElseThrow().getPublishStatus().orElseThrow())
                .isEqualTo(BlockchainPublishStatus.ROLLBACKED);
    }

}
