package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.helper.MetadataToJsonNoSchemaConverter;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.google.common.collect.Sets;
import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_common.service_assistance.MetadataChecker;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainTransactions;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApi;
import org.zalando.problem.Problem;

import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.iterators.PeekingIterator.peekingIterator;

@Slf4j
@RequiredArgsConstructor
public class L1TransactionCreator {

    private static final int CARDANO_MAX_TRANSACTION_SIZE_BYTES = 16384;

    private final BackendService backendService;

    private final MetadataSerialiser metadataSerialiser;

    private final BlockchainReaderPublicApi blockchainReaderPublicApi;

    private final MetadataChecker jsonSchemaMetadataChecker;

    private final Account organiserAccount;

    private final int metadataLabel;

    private final boolean debugStoreOutputTx;

    private String runId;

    @PostConstruct
    public void init() {
        log.info("L1TransactionCreator::metadata label: {}", metadataLabel);
        log.info("L1TransactionCreator::debug store output tx: {}", debugStoreOutputTx);

        runId = UUID.randomUUID().toString();
        log.info("L1TransactionCreator::runId: {}", runId);

        log.info("L1TransactionCreator is initialised.");
    }

    public Either<Problem, Optional<BlockchainTransactions>> pullBlockchainTransaction(String organisationId,
                                                                                       Set<TransactionEntity> txs) {
        val chainTipE = blockchainReaderPublicApi.getChainTip();

        return chainTipE.map(chainTip -> createTransaction(organisationId, txs, chainTip.getAbsoluteSlot()));
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
        try {
            val metadataMap =
                    metadataSerialiser.serialiseToMetadataMap(organisationId, transactionsBatch, creationSlot);

            val data = metadataMap.getMap();
            val bytes = CborSerializationUtil.serialize(data);

            // we use json only for validation with json schema and for debugging (storing to a tmp file)
            val json = MetadataToJsonNoSchemaConverter.cborBytesToJson(bytes);

            val metadata = MetadataBuilder.createMetadata();
            metadata.put(metadataLabel, metadataMap);

            if (debugStoreOutputTx) {
                val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
                val name = STR."lob-tx-metadata-\{runId}-\{timestamp}-\{creationSlot}";
                val tmpTxFile = Files.createTempFile(name, ".json");

                log.info("DebugStoreTx enabled, storing tx metadata to file: {}", tmpTxFile);

                Files.writeString(tmpTxFile, json);
            }

            val isValid = jsonSchemaMetadataChecker.checkTransactionMetadata(json);

            if (!isValid) {
                return Either.left(Problem.builder()
                        .withTitle("INVALID_TRANSACTION_METADATA")
                        .withDetail("Metadata is not valid according to the transaction schema, we will not create a transaction!")
                        .build()
                );
            }

            log.info("Metadata for tx validated, gonna serialise tx now...");

            return Either.right(serialiseTransaction(metadata));
        } catch (Exception e) {
            log.error("Error serialising metadata to cbor", e);
            return Either.left(Problem.builder()
                    .withTitle("ERROR_SERIALISING_METADATA")
                    .withDetail("Error serialising metadata to cbor")
                    .build()
            );
        }
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
