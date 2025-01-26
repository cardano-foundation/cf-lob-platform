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
import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_common.service_assistance.MetadataChecker;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.API3BlockchainTransaction;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.SerializedCardanoL1Transaction;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.reports.ReportEntity;
import org.cardanofoundation.lob.app.blockchain_reader.BlockchainReaderPublicApiIF;
import org.zalando.problem.Problem;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

@RequiredArgsConstructor
@Slf4j
public class API3L1TransactionCreator {

    private final BackendService backendService;
    private final API3MetadataSerialiser api3MetadataSerialiser;
    private final BlockchainReaderPublicApiIF blockchainReaderPublicApi;
    private final MetadataChecker jsonSchemaMetadataChecker;
    private final Account organiserAccount;

    private final int metadataLabel;
    private final boolean debugStoreOutputTx;

    private String runId;

    @PostConstruct
    public void init() {
        log.info("API3L1TransactionCreator::metadata label: {}", metadataLabel);
        log.info("API3L1TransactionCreator::debug store output tx: {}", debugStoreOutputTx);

        runId = UUID.randomUUID().toString();
        log.info("API3L1TransactionCreator::runId: {}", runId);

        log.info("API3L1TransactionCreator is initialised.");
    }

    public Either<Problem, API3BlockchainTransaction> pullBlockchainTransaction(ReportEntity reportEntity) {
        return blockchainReaderPublicApi.getChainTip()
                .flatMap(chainTip -> handleTransactionCreation(reportEntity, chainTip.getAbsoluteSlot()));
    }

    private Either<Problem, API3BlockchainTransaction> handleTransactionCreation(ReportEntity reportEntity,
                                                                                 long creationSlot) {
        try {
            val metadataMap =
                    api3MetadataSerialiser.serialiseToMetadataMap(reportEntity, creationSlot);

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

            val serialisedTxBytes = serialiseTransaction(metadata);

            val serializedTx = new SerializedCardanoL1Transaction(serialisedTxBytes, bytes, json);

            potentiallyStoreTxs(creationSlot, serializedTx);

            return Either.right(new API3BlockchainTransaction(reportEntity, creationSlot, serialisedTxBytes, organiserAccount.baseAddress()));
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

    // for debug and inspection only
    private void potentiallyStoreTxs(long creationSlot, SerializedCardanoL1Transaction tx) throws IOException {
        if (debugStoreOutputTx) {
            val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            val name = STR."lob-txs-api3-metadata-\{runId}-\{timestamp}-\{creationSlot}";
            val tmpJsonTxFile = Files.createTempFile(name, ".json");
            val tmpCborFile = Files.createTempFile(name, ".cbor");

            log.info("DebugStoreTx enabled, storing JSON tx metadata to file: {}", tmpJsonTxFile);
            Files.writeString(tmpJsonTxFile, tx.metadataJson());

            log.info("DebugStoreTx enabled, storing CBOR tx metadata to file: {}", tmpCborFile);
            Files.write(tmpCborFile, tx.metadataCbor());
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
