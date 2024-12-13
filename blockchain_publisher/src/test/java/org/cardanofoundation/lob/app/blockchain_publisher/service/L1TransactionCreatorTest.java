//package org.cardanofoundation.lob.app.blockchain_publisher.service;
//
//import com.bloxbean.cardano.client.account.Account;
//import com.bloxbean.cardano.client.backend.api.BackendService;
//import com.bloxbean.cardano.client.common.model.Networks;
//import io.vavr.control.Either;
//import lombok.val;
//import org.cardanofoundation.lob.app.blockchain_common.service_assistance.MetadataChecker;
//import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainTransactions;
//import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionEntity;
//import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApiIF;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.zalando.problem.Problem;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.doReturn;
//
//@ExtendWith(MockitoExtension.class)
//public class L1TransactionCreatorTest {
//
//    @Mock
//    private BackendService backendService;
//
//    @Mock
//    private MetadataSerialiser metadataSerialiser;
//
//    @Mock
//    private BlockchainReaderPublicApiIF blockchainReaderPublicApi;
//
//    @Mock
//    private MetadataChecker jsonSchemaMetadataChecker;
//
//    @InjectMocks
//    private L1TransactionCreator transactionCreator;
//
//    private final int smallTransactionSize = 5000;
//    private final int maxTransactionSize = 10000;
//
//    @BeforeEach
//    void setUp() {
//        transactionCreator = new L1TransactionCreator(
//                backendService,
//                metadataSerialiser,
//                blockchainReaderPublicApi,
//                jsonSchemaMetadataChecker,
//                new Account(Networks.testnet(), "test test test test test test test test test test test test test test test test test test test test test test test sauce"),
//                1447,
//                false
//        );
//    }
//
//    @Test
//    void shouldReturnProblemWhenChainTipFails() {
//        given(blockchainReaderPublicApi.getChainTip()).willReturn(Either.left(any(Problem.class)));
//
//        Either<Problem, Optional<BlockchainTransactions>> result = transactionCreator.pullBlockchainTransaction("org1", createMockTransactions(5));
//
//        assertThat(result.isLeft()).isTrue();
//        assertThat(result.getLeft().getDetail()).isEqualTo("Failed to fetch chain tip");
//    }
//
//    @Test
//    void shouldCreateTransactionWhenBatchSizeDoesNotExceedLimit() throws IOException {
//        val transactions = createMockTransactions(2);
//        mockSerializationWithSize(smallTransactionSize);
//
//        Either<Problem, Optional<BlockchainTransactions>> result = transactionCreator.pullBlockchainTransaction("org1", transactions);
//
//        assertThat(result.isRight()).isTrue();
//        assertThat(result.get()).isPresent();
//        assertThat(result.get().get().getTransactions()).containsAll(transactions);
//    }
//
//    @Test
//    void shouldSplitIntoMultipleTransactionsWhenBatchSizeExceedsLimit() throws IOException {
//        Set<TransactionEntity> transactions = createMockTransactions(5);
//        mockSerializationWithSize(maxTransactionSize - 1); // Ensure each tx nearly fills the max size limit
//
//        Either<Problem, Optional<BlockchainTransactions>> result = transactionCreator.pullBlockchainTransaction("org1", transactions);
//
//        assertThat(result.isRight()).isTrue();
//        assertThat(result.get()).isPresent();
//        BlockchainTransactions blockchainTransactions = result.get().get();
//        assertThat(blockchainTransactions.getTransactions()).hasSize(1); // First batch contains only 1 transaction
//    }
//
//    @Test
//    void shouldHandleRemainingTransactionsWhenLeftover() throws IOException {
//        Set<TransactionEntity> transactions = createMockTransactions(3);
//        mockSerializationWithSize(smallTransactionSize);
//
//        Either<Problem, Optional<BlockchainTransactions>> result = transactionCreator.pullBlockchainTransaction("org1", transactions);
//
//        assertThat(result.isRight()).isTrue();
//        assertThat(result.get()).isPresent();
//        BlockchainTransactions blockchainTransactions = result.get().get();
//        assertThat(blockchainTransactions.getRemainingTransactions()).hasSize(2); // 2 transactions remaining after creating the first batch
//    }
//
//    @Test
//    void shouldReturnEmptyWhenNoTransactionsLeft() {
//        Either<Problem, Optional<BlockchainTransactions>> result = transactionCreator.pullBlockchainTransaction("org1", new LinkedHashSet<>());
//
//        assertThat(result.isRight()).isTrue();
//        assertThat(result.get()).isEmpty();
//    }
//
//    private Set<TransactionEntity> createMockTransactions(int count) {
//        Set<TransactionEntity> transactions = new LinkedHashSet<>();
//        for (int i = 0; i < count; i++) {
//            val transaction = new org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity();
//            transaction.setTransactionInternalNumber(STR."txNumber-\{i}");
//            //transaction.setItems(items);
//            transaction.setViolations(new HashSet<>());
//        }
//
//        return transactions;
//    }
//
//    private void mockSerializationWithSize(int size) throws IOException {
//        val serializedTx = new L1TransactionCreator.SerializedCardanoL1Transaction(new byte[size], new byte[size], "{}");
//        doReturn(Either.right(serializedTx)).when(metadataSerialiser).serialiseToMetadataMap(any(), any(), anyLong());
//        given(jsonSchemaMetadataChecker.checkTransactionMetadata(any())).willReturn(true);
//    }
//
//}
