package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.google.common.collect.Sets;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainTransactions;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.iterators.PeekingIterator.peekingIterator;

@Service
@Slf4j
@RequiredArgsConstructor
public class L1TransactionCreator {

    private static final int CARDANO_MAX_TRANSACTION_SIZE_BYTES = 16384;

    private final BackendService backendService;

    private final MetadataSerialiser metadataSerialiser;

    private final BlockchainDataChainTipService blockchainDataChainTipService;

    private final MetadataChecker jsonSchemaMetadataChecker;

    private final Account organiserAccount;

    @Value("${l1.transaction.metadata.label:22222}")
    private int metadataLabel;

    public Either<Problem, Optional<BlockchainTransactions>> pullBlockchainTransaction(String organisationId,
                                                                                       Set<TransactionEntity> txs) {
        val chainTipE = blockchainDataChainTipService.latestChainTip();

        return chainTipE.map(chainTip -> createTransaction(organisationId, txs, chainTip.absoluteSlot()));
    }

    private Optional<BlockchainTransactions> createTransaction(String organisationId,
                                                               Set<TransactionEntity> transactions,
                                                               long creationSlot) {
        log.info("Splitting {} passedTransactions into blockchain passedTransactions", transactions.size());

        val transactionsBatch = new LinkedHashSet<TransactionEntity>();

        for (var it = peekingIterator(transactions.iterator()); it.hasNext();) {
            val txEntity = it.next();

            transactionsBatch.add(txEntity);

            val txBytesE = serialiseTransactionChunk(organisationId, transactionsBatch, creationSlot);
            if (txBytesE.isLeft()) {
                log.error("Error serialising transaction, abort processing, issue: {}", txBytesE.getLeft().getDetail());
                return Optional.empty();
            }

            val txBytes = txBytesE.get();

            val transactionLinePeek = it.peek();
            if (transactionLinePeek == null) { // next one is last element
                continue;
            }
            val newChunkTxBytesE = serialiseTransactionChunk(organisationId, Stream.concat(transactionsBatch.stream(), Stream.of(transactionLinePeek)).collect(Collectors.toSet()), creationSlot);
            if (newChunkTxBytesE.isLeft()) {
                log.error("Error serialising transaction, abort processing, issue: {}", newChunkTxBytesE.getLeft().getDetail());
                return Optional.empty();
            }
            val newChunkTxBytes = newChunkTxBytesE.get();

            if (newChunkTxBytes.length >= CARDANO_MAX_TRANSACTION_SIZE_BYTES) {
                log.info("Blockchain transaction created, id:{}", TransactionUtil.getTxHash(txBytes));

                final var remaining = calculateRemainingTransactionLines(transactions, transactionsBatch);

                return Optional.of(new BlockchainTransactions(organisationId, transactionsBatch, remaining, creationSlot, txBytes));
            }
        }

        // if there are any left overs
        if (!transactionsBatch.isEmpty()) {
            log.info("Last batch size: {}", transactionsBatch.size());

            val txBytesE = serialiseTransactionChunk(organisationId, transactionsBatch, creationSlot);

            if (txBytesE.isEmpty()) {
                log.error("Error serialising transaction, abort processing, issue: {}", txBytesE.getLeft().getDetail());
                return Optional.empty();
            }

            val txBytes = txBytesE.get();

            log.info("Transaction size: {}", txBytes.length);

            final var remaining = calculateRemainingTransactionLines(transactions, transactionsBatch);

            return Optional.of(new BlockchainTransactions(organisationId, transactionsBatch, remaining, creationSlot, txBytes));
        }

        return Optional.empty();
    }

    private static Set<TransactionEntity> calculateRemainingTransactionLines(
            Set<TransactionEntity> transactions,
            Set<TransactionEntity> transactionsBatch) {

        return Sets.difference(transactions, transactionsBatch);
    }

    private Either<Problem, byte[]> serialiseTransactionChunk(String organisationId,
                                                              Set<TransactionEntity> transactionsBatch,
                                                              long creationSlot) {
        val metadataMap =
                metadataSerialiser.serialiseToMetadataMap(organisationId, transactionsBatch, creationSlot);

//        try {
//            System.out.println(metadataMap.toJson());
//        } catch (CborException e) {
//            throw new RuntimeException(e);
//        }

        val metadata = MetadataBuilder.createMetadata();
        metadata.put(metadataLabel, metadataMap);

        val isValid = jsonSchemaMetadataChecker.checkTransactionMetadata(metadataMap);

        if (!isValid) {
            return Either.left(Problem.builder()
                    .withTitle("INVALID_TRANSACTION_METADATA")
                    .withDetail("Metadata is not valid according to the transaction schema!")
                    .build()
            );
        }

        return Either.right(serialiseTransaction(metadata));
    }

    @SneakyThrows
    protected byte[] serialiseTransaction(Metadata metadata) {
        val quickTxBuilder = new QuickTxBuilder(backendService);

        val tx = new Tx()
                .payToAddress(organiserAccount.baseAddress(), Amount.ada(2.0))
                .attachMetadata(metadata)
                .from(organiserAccount.baseAddress());

        return quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(organiserAccount))
                .buildAndSign()
                .serialize();
    }

}
