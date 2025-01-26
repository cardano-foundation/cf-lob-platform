package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.metadata.helper.MetadataToJsonNoSchemaConverter;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.google.common.collect.Sets;
import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_common.service_assistance.MetadataChecker;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.API1BlockchainTransactions;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.SerializedCardanoL1Transaction;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.txs.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApiIF;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.io.IOException;
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
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
public class API1L1TransactionCreator {

    private static final int CARDANO_MAX_TRANSACTION_SIZE_BYTES = 16384;

    private final BackendService backendService;
    private final API1MetadataSerialiser api1MetadataSerialiser;
    private final BlockchainReaderPublicApiIF blockchainReaderPublicApi;
    private final MetadataChecker jsonSchemaMetadataChecker;
    private final Account organiserAccount;

    private final int metadataLabel;
    private final boolean debugStoreOutputTx;

    private String runId;

    @PostConstruct
    public void init() {
        log.info("API1L1TransactionCreator::metadata label: {}", metadataLabel);
        log.info("API1L1TransactionCreator::debug store output tx: {}", debugStoreOutputTx);

        runId = UUID.randomUUID().toString();
        log.info("API1L1TransactionCreator::runId: {}", runId);

        log.info("API1L1TransactionCreator is initialised.");
    }

    public Either<Problem, Optional<API1BlockchainTransactions>> pullBlockchainTransaction(String organisationId,
                                                                                           Set<TransactionEntity> txs) {
        return blockchainReaderPublicApi.getChainTip()
                .flatMap(chainTip -> handleTransactionCreation(organisationId, txs, chainTip.getAbsoluteSlot()));
    }

    private Either<Problem, Optional<API1BlockchainTransactions>> handleTransactionCreation(String organisationId,
                                                                                            Set<TransactionEntity> transactions,
                                                                                            long creationSlot) {
        try {
            return createTransaction(organisationId, transactions, creationSlot);
        } catch (IOException e) {
            log.error("Error creating blockchain transaction: ", e);

            return Either.left(Problem.builder()
                    .withTitle("ERROR_CREATING_TRANSACTION")
                    .withDetail(STR."Exception encountered: \{e.getMessage()}")
                    .withStatus(INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    // error or transactions to process or no more transactions to process in case of blockchain transaction creation
    private Either<Problem, Optional<API1BlockchainTransactions>> createTransaction(String organisationId,
                                                                                    Set<TransactionEntity> transactions,
                                                                                    long creationSlot) throws IOException {
        log.info("Splitting {} passedTransactions into blockchain passedTransactions", transactions.size());

        val transactionsBatch = new LinkedHashSet<TransactionEntity>();

        for (var it = peekingIterator(transactions.iterator()); it.hasNext();) {
            val txEntity = it.next();

            transactionsBatch.add(txEntity);

            val serializedTransactionsE = serialiseTransactionChunk(organisationId, transactionsBatch, creationSlot);
            if (serializedTransactionsE.isLeft()) {
                log.error("Error serialising transaction, abort processing, issue: {}", serializedTransactionsE.getLeft().getDetail());

                return Either.left(serializedTransactionsE.getLeft());
            }

            val serializedTransaction = serializedTransactionsE.get();
            val txBytes = serializedTransaction.txBytes();

            val transactionLinePeek = it.peek();
            if (transactionLinePeek == null) { // next one is last element
                continue;
            }
            val newChunkTxBytesE = serialiseTransactionChunk(organisationId, Stream.concat(transactionsBatch.stream(), Stream.of(transactionLinePeek))
                    .collect(Collectors.toSet()), creationSlot);

            if (newChunkTxBytesE.isLeft()) {
                log.error("Error serialising transaction, abort processing, issue: {}", newChunkTxBytesE.getLeft().getDetail());

                return Either.left(newChunkTxBytesE.getLeft());
            }
            val newSerializedTransaction = newChunkTxBytesE.get();
            val newChunkTxBytes = newSerializedTransaction.txBytes();

            if (newChunkTxBytes.length >= CARDANO_MAX_TRANSACTION_SIZE_BYTES) {
                log.info("Blockchain transaction created, id:{}", TransactionUtil.getTxHash(txBytes));

                log.info("Blockchain transaction created, id:{}, debugTxOutput:{}", TransactionUtil.getTxHash(txBytes), this.debugStoreOutputTx);
                potentiallyStoreTxs(creationSlot, serializedTransaction);

                val remainingTxs = calculateRemainingTransactions(transactions, transactionsBatch);

                return Either.right(Optional.of(new API1BlockchainTransactions(organisationId, transactionsBatch, remainingTxs, creationSlot, txBytes, organiserAccount.baseAddress())));
            }
        }

        // if there are any left overs, meaning that the batch is not full, e.g. just a couple of transactions to serialise
        if (!transactionsBatch.isEmpty()) {
            log.info("Leftovers batch size: {}", transactionsBatch.size());

            val serializedTxE = serialiseTransactionChunk(organisationId, transactionsBatch, creationSlot);

            if (serializedTxE.isEmpty()) {
                log.error("Error serialising transaction, abort processing, issue: {}", serializedTxE.getLeft().getDetail());

                return Either.left(serializedTxE.getLeft());
            }

            val serTx = serializedTxE.get();
            val txBytes = serTx.txBytes();
            log.info("Blockchain transaction created, id:{}, debugTxOutput:{}", TransactionUtil.getTxHash(txBytes), this.debugStoreOutputTx);

            potentiallyStoreTxs(creationSlot, serTx);

            log.info("Transaction size: {}", txBytes.length);

            val remaining = calculateRemainingTransactions(transactions, transactionsBatch);

            return Either.right(Optional.of(new API1BlockchainTransactions(
                    organisationId,
                    transactionsBatch,
                    remaining,
                    creationSlot,
                    txBytes,
                    organiserAccount.baseAddress()
            )));
        }

        // no transactions to process
        return Either.right(Optional.empty());
    }

    // for debug and inspection only
    private void potentiallyStoreTxs(long creationSlot, SerializedCardanoL1Transaction tx) throws IOException {
        if (debugStoreOutputTx) {
            val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            val name = STR."lob-txs-api1-metadata-\{runId}-\{timestamp}-\{creationSlot}";
            val tmpJsonTxFile = Files.createTempFile(name, ".json");
            val tmpCborFile = Files.createTempFile(name, ".cbor");

            log.info("DebugStoreTx enabled, storing JSON tx metadata to file: {}", tmpJsonTxFile);
            Files.writeString(tmpJsonTxFile, tx.metadataJson());

            log.info("DebugStoreTx enabled, storing CBOR tx metadata to file: {}", tmpCborFile);
            Files.write(tmpCborFile, tx.metadataCbor());
        }
    }

    private static Set<TransactionEntity> calculateRemainingTransactions(
            Set<TransactionEntity> transactions,
            Set<TransactionEntity> transactionsBatch) {

        return Sets.difference(transactions, transactionsBatch);
    }

    private Either<Problem, SerializedCardanoL1Transaction> serialiseTransactionChunk(String organisationId,
                                                                                      Set<TransactionEntity> transactionsBatch,
                                                                                      long creationSlot) {
        try {
            val metadataMap =
                    api1MetadataSerialiser.serialiseToMetadataMap(organisationId, transactionsBatch, creationSlot);

            val data = metadataMap.getMap();
            val bytes = CborSerializationUtil.serialize(data);

            // we use json only for validation with json schema and for debugging (storing to a tmp file)
            val json = MetadataToJsonNoSchemaConverter.cborBytesToJson(bytes);

            val metadata = MetadataBuilder.createMetadata();
            val cborMetadataMap = new CBORMetadataMap(data);

            metadata.put(metadataLabel, cborMetadataMap);

            val isValid = jsonSchemaMetadataChecker.checkTransactionMetadata(json);

            if (!isValid) {
                return Either.left(Problem.builder()
                        .withTitle("INVALID_TRANSACTION_METADATA")
                        .withDetail("Metadata is not valid according to the transaction schema, we will not create a transaction!")
                        .withStatus(INTERNAL_SERVER_ERROR)
                        .build()
                );
            }

            log.info("Metadata for tx validated, gonna serialise tx now...");

            val serialisedTx = serialiseTransaction(metadata);

            return Either.right(new SerializedCardanoL1Transaction(serialisedTx, bytes, json));
        } catch (Exception e) {
            log.error("Error serialising metadata to cbor", e);
            return Either.left(Problem.builder()
                    .withTitle("ERROR_SERIALISING_METADATA")
                    .withDetail("Error serialising metadata to cbor")
                    .withStatus(INTERNAL_SERVER_ERROR)
                    .build()
            );
        }
    }

    protected byte[] serialiseTransaction(Metadata metadata) throws CborSerializationException {
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
