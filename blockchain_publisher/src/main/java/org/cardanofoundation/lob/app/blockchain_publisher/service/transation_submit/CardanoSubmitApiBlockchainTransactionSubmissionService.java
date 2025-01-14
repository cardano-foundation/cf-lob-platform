package org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit;

import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.client.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_common.BlockchainException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@RequiredArgsConstructor
public class CardanoSubmitApiBlockchainTransactionSubmissionService implements BlockchainTransactionSubmissionService {

    private final String cardanoSubmitApiUrl;
    private final String blockfrostApiKey;

    private final HttpClient httpClient;
    private final int timeoutInSeconds;

    @Override
    @SneakyThrows
    public String submitTransaction(byte[] txData) {
        val txTransactionSubmitPostRequest = HttpRequest.newBuilder()
                .uri(URI.create(cardanoSubmitApiUrl))
                .POST(HttpRequest.BodyPublishers.ofByteArray(txData))
                .timeout(java.time.Duration.ofSeconds(timeoutInSeconds))
                .header("Content-Type", "application/cbor")
                .header("project_id", blockfrostApiKey)
                .build();

        val r = httpClient.send(txTransactionSubmitPostRequest, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            val json = r.body();

            val jNode = JsonUtil.parseJson(json);
            val txId  = jNode.asText();

            return txId;
        }

        throw new BlockchainException(STR."Error submitting transaction: \{TransactionUtil.getTxHash(txData)} to CardanoSubmitApi. Response: \{r.statusCode()} - \{r.body()}");
    }

}
