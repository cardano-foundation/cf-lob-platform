package org.cardano.foundation.lob.shell;

import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.metadata.helper.MetadataToJsonNoSchemaConverter;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.util.JsonUtil;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ShellComponent
@Slf4j
@RequiredArgsConstructor
public class API1SubmissionCommands2024 {

    private final BackendService backendService;
    private final Account organiserAccount;

    @Value("${l1.transaction.metadata_label:1447}")
    private int metadataLabel;

    @ShellMethod(key = "03_submit-txs", value = "Submit API1 txs")
    @Order(3)
    public String submitAPi1Transactions(
            @ShellOption(help = "folder name within data directory where cbor for transactions is") String suppliedFolderName,
            @ShellOption(help = "runId") String suppliedRunId
    ) throws Exception {
        log.info("API1, specified subfolder within data, folder: {}, runId:{}", suppliedFolderName, suppliedRunId);

        val file = new File("data/" + suppliedFolderName);
        if (!file.exists()) {
            throw new Exception("Folder does not exist: " + file.getAbsolutePath());
        }

        val directory = Paths.get(file.getAbsolutePath());
        val extension = ".cbor";

        // Define regex to extract timestamp
        val timestampPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z");

        var txCounts = 0;

        val txMetadataList = new ArrayList<Metadata>();
        try (Stream<Path> files = Files.walk(directory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(extension))
                    .filter(path -> {
                        // Filter by runId
                        String fileName = path.getFileName().toString();

                        return fileName.contains(suppliedRunId);
                    })
                    .sorted(Comparator.comparing(path -> {
                        // Extract timestamp from the filename and convert to millis
                        String fileName = path.getFileName().toString();
                        Matcher matcher = timestampPattern.matcher(fileName);
                        if (matcher.find()) {
                            String timestamp = matcher.group();
                            return Instant.parse(timestamp).toEpochMilli();
                        } else {
                            throw new RuntimeException("Invalid filename, no timestamp found: " + fileName);
                        }
                    }))
                    .map(path -> {
                        try {
                            byte[] txMetadataCbor = Files.readAllBytes(path);
                            val dataItem = (Map) CborSerializationUtil.deserialize(txMetadataCbor);
                            val cborMetadataMap = new CBORMetadataMap(dataItem);

                            val metadata = MetadataBuilder.createMetadata();
                            metadata.put(metadataLabel, cborMetadataMap);

                            val json = MetadataToJsonNoSchemaConverter.cborBytesToJson(txMetadataCbor);

                            log.info("Physical Tx JSON: {}", json);

                            return metadata;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEachOrdered(txMetadataList::add);
        }

        log.info("API1, txMetadataList size: {}", txMetadataList.size());

        for (Metadata metadata : txMetadataList) {
            val txResultE = submitL1Tx(metadata);

            if (txResultE.isLeft()) {
                throw new RuntimeException("Error serialising transaction, error: ", txResultE.getLeft());
            }

            try {
                val txResult = txResultE.get();

                if (!txResult.isSuccessful()) {
                    throw new RuntimeException("Error composing transaction, response:" + txResult.getResponse());
                }

                log.info("Transaction submitted and confirmed. tx hash: {}", txResult.getValue());

                txCounts++;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return "API1 transactions submitted, L1 physical txs count: " + txCounts;
    }

    protected Either<Exception, Result<String>> submitL1Tx(Metadata metadata) {
        val quickTxBuilder = new QuickTxBuilder(backendService);

        val senderAddress = organiserAccount.baseAddress();
        val tx = new Tx()
                .payToAddress(organiserAccount.baseAddress(), Amount.ada(1))
                .attachMetadata(metadata)
                .from(senderAddress);

        try {
            val txResult = quickTxBuilder.compose(tx)
                    //.feePayer(organiserAccount.baseAddress())
                    .withSigner(SignerProviders.signerFrom(organiserAccount))
                    //.buildAndSign()
                    .completeAndWait(Duration.ofMinutes(15));

            waitForTransaction(txResult);
            checkIfUtxoAvailable(txResult.getValue(), senderAddress);

            return Either.right(txResult);
        } catch (Exception e) {
            log.error("Error sending transaction", e);

            return Either.left(e);
        }
    }

    public void waitForTransaction(Result<String> result) {
        try {
            if (result.isSuccessful()) { //Wait for transaction to be mined
                int count = 0;
                while (count < 60) {
                    Result<TransactionContent> txnResult = backendService.getTransactionService().getTransaction(result.getValue());
                    if (txnResult.isSuccessful()) {
                        System.out.println("Transaction mined, txHash:" + result.getValue());
                        break;
                    } else {
                        System.out.println("Waiting for transaction to be mined ....");
                    }

                    count++;
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for transaction to be mined", e);
        }
    }

    protected void checkIfUtxoAvailable(String txHash, String address) {
        Optional<Utxo> utxo = Optional.empty();
        int count = 0;

        while (utxo.isEmpty()) {
            if (count++ >= 20)
                break;
            List<Utxo> utxos = new DefaultUtxoSupplier(backendService.getUtxoService()).getAll(address);
            utxo = utxos.stream().filter(u -> u.getTxHash().equals(txHash))
                    .findFirst();
            System.out.println("Try to get new output... txhash: " + txHash);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
        }
    }

}
