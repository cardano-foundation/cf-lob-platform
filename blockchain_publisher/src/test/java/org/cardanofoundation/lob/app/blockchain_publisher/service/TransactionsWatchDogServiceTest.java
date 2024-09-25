import io.vavr.control.Either;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_common.domain.CardanoNetwork;
import org.cardanofoundation.lob.app.blockchain_common.domain.ChainTip;
import org.cardanofoundation.lob.app.blockchain_common.domain.FinalityScore;
import org.cardanofoundation.lob.app.blockchain_common.domain.OnChainTxDetails;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainPublishStatusMapper;
import org.cardanofoundation.lob.app.blockchain_publisher.service.TransactionsWatchDogService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.event_publish.LedgerUpdatedEventPublisher;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApiIF;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.junit.jupiter.api.BeforeEach;
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
    private BlockchainReaderPublicApiIF blockchainReaderPublicApi;

    @Mock
    private OrganisationPublicApiIF organisationPublicApiIF;

    @Mock
    private LedgerUpdatedEventPublisher ledgerUpdatedEventPublisher;

    @Mock
    private BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    @InjectMocks
    private TransactionsWatchDogService transactionsWatchDogService;

    @BeforeEach
    public void setup() {
        transactionsWatchDogService.setRollbackGracePeriodMinutes(1); // reduce grace period from ca 30 minutes to 1 minute for unit testing purpouses
    }

    // Test case for an empty organization list
    @Test
    void testCheckTransactionStatusesForOrganisation_returnsNonEmptyList() {
        // Arrange
        val organisation1 = new Organisation();
        organisation1.setId("org1");

        when(organisationPublicApiIF.listAll()).thenReturn(List.of(organisation1));

        when(transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(eq("org1"), any(Limit.class)))
                .thenReturn(emptySet());

        when(blockchainReaderPublicApi.getChainTip()).thenReturn(Either.right(new ChainTip(1L, "hash1", Optional.of(1), CardanoNetwork.DEV, true)));

        // Act
        List<Either<Problem, Set<TransactionEntity>>> result = transactionsWatchDogService.checkTransactionStatusesForOrganisation(Integer.MAX_VALUE);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).isRight()).isTrue();  // Ensure the result is a successful response
    }

    // Test case where the chain tip cannot be found (either left side of Either)
    @Test
    void testInspectOrganisationTransactions_withEmptyChainTip() {
        // Arrange
        when(blockchainReaderPublicApi.getChainTip()).thenReturn(Either.left(Problem.builder()
                .withTitle("CHAIN_TIP_NOT_FOUND")
                .build()));

        // Act
        val result = transactionsWatchDogService.inspectOrganisationTransactions("org1", Integer.MAX_VALUE);

        // Assert
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getTitle()).isEqualTo("CHAIN_TIP_NOT_FOUND");
    }

    // Test case where the transaction is on-chain and finality progresses
    @Test
    void testInspectOrganisationTransactions_withOnChainDataCompleted() {
        val chainTip = 1000L;
        val txAbsoluteSlotNow = chainTip - 50;
        val txAbsoluteSlotThen = chainTip - 500;
        val txCreationSlow = txAbsoluteSlotNow - 20;

        // Arrange
        val l1SubmissionData = new L1SubmissionData();
        l1SubmissionData.setTransactionHash(Optional.of("hash1"));
        l1SubmissionData.setAbsoluteSlot(Optional.of(txAbsoluteSlotThen));
        l1SubmissionData.setPublishStatus(Optional.of(BlockchainPublishStatus.VISIBLE_ON_CHAIN));
        l1SubmissionData.setCreationSlot(Optional.of(txCreationSlow));

        val transactionEntity = new TransactionEntity();
        transactionEntity.setL1SubmissionData(Optional.of(l1SubmissionData));

        when(transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(eq("org1"), any(Limit.class)))
                .thenReturn(Set.of(transactionEntity));

        when(blockchainReaderPublicApi.getChainTip()).thenReturn(Either.right(new ChainTip(chainTip, "hash1", Optional.of(1), CardanoNetwork.DEV, true)));

        // return latest on-chain data
        when(blockchainReaderPublicApi.getTxDetails(l1SubmissionData.getTransactionHash().orElseThrow()))
                .thenReturn(Either.right(Optional.of(OnChainTxDetails.builder()
                        .transactionHash("hash1")
                        .blockHash("blockHash1")
                        .absoluteSlot(txAbsoluteSlotNow)
                        .finalityScore(FinalityScore.VERY_HIGH)
                        .network(CardanoNetwork.DEV)
                        .build()))
                );

        when(blockchainPublishStatusMapper.convert(any(FinalityScore.class))).thenReturn(BlockchainPublishStatus.COMPLETED);

        // Act
        val result = transactionsWatchDogService.inspectOrganisationTransactions("org1", Integer.MAX_VALUE);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).contains(transactionEntity);

        assertThat(transactionEntity.getL1SubmissionData().orElseThrow().getPublishStatus().orElseThrow())
                .isEqualTo(BlockchainPublishStatus.COMPLETED);
    }

    // Test case where the transaction is on-chain and finality is fully achieved
    @Test
    void testInspectOrganisationTransactions_withOnChainDataFinalized() {
        val chainTip = 1000L;
        val txAbsoluteSlotNow = chainTip - 2160;
        val txAbsoluteSlotThen = chainTip - 999;
        val txCreationSlow = txAbsoluteSlotNow - 20;

        // Arrange
        val l1SubmissionData = new L1SubmissionData();
        l1SubmissionData.setTransactionHash(Optional.of("hash1"));
        l1SubmissionData.setAbsoluteSlot(Optional.of(txAbsoluteSlotThen));
        l1SubmissionData.setPublishStatus(Optional.of(BlockchainPublishStatus.VISIBLE_ON_CHAIN));
        l1SubmissionData.setCreationSlot(Optional.of(txCreationSlow));

        val transactionEntity = new TransactionEntity();
        transactionEntity.setL1SubmissionData(Optional.of(l1SubmissionData));

        when(transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(eq("org1"), any(Limit.class)))
                .thenReturn(Set.of(transactionEntity));

        when(blockchainReaderPublicApi.getChainTip()).thenReturn(Either.right(new ChainTip(chainTip, "hash2", Optional.of(1), CardanoNetwork.DEV, true)));

        // return latest on-chain data
        when(blockchainReaderPublicApi.getTxDetails(l1SubmissionData.getTransactionHash().orElseThrow()))
                .thenReturn(Either.right(Optional.of(OnChainTxDetails.builder()
                        .transactionHash("hash1")
                        .blockHash("blockHash1")
                        .absoluteSlot(txAbsoluteSlotNow)
                        .finalityScore(FinalityScore.FINAL)
                        .network(CardanoNetwork.DEV)
                        .build()))
                );

        when(blockchainPublishStatusMapper.convert(any(FinalityScore.class))).thenReturn(BlockchainPublishStatus.FINALIZED);

        // Act
        val result = transactionsWatchDogService.inspectOrganisationTransactions("org1", Integer.MAX_VALUE);

        // Assert
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).contains(transactionEntity);

        assertThat(transactionEntity.getL1SubmissionData().orElseThrow().getPublishStatus().orElseThrow())
                .isEqualTo(BlockchainPublishStatus.FINALIZED);
    }

    // Test case for rollback scenario where the transaction is no longer found on-chain
    @Test
    void testInspectOrganisationTransactions_withRollbackScenario() {
        val chainTip = 100L;
        val absoluteSlot = chainTip - 80;
        val creationSlot = absoluteSlot - 20;

        val txSubmissionData = new L1SubmissionData();
        txSubmissionData.setPublishStatus(Optional.of(BlockchainPublishStatus.VISIBLE_ON_CHAIN));
        txSubmissionData.setFinalityScore(Optional.of(FinalityScore.LOW));
        txSubmissionData.setTransactionHash(Optional.of("hash1"));
        txSubmissionData.setAbsoluteSlot(Optional.of(absoluteSlot));
        txSubmissionData.setCreationSlot(Optional.of(creationSlot));

        // Arrange
        val transactionEntity = new TransactionEntity();
        transactionEntity.setId("tx1");
        transactionEntity.setL1SubmissionData(Optional.of(txSubmissionData));

        when(transactionEntityRepositoryGateway.findDispatchedTransactionsThatAreNotFinalizedYet(eq("org1"), any(Limit.class)))
                .thenReturn(Set.of(transactionEntity));

        when(blockchainReaderPublicApi.getChainTip()).thenReturn(Either.right(new ChainTip(chainTip, "hash2", Optional.of(1), CardanoNetwork.DEV, true)));
        when(blockchainReaderPublicApi.getTxDetails(txSubmissionData.getTransactionHash().orElseThrow()))
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
