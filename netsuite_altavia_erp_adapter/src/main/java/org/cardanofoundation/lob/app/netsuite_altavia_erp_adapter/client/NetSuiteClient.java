package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.scribe.model.Verb.GET;

@Slf4j
@RequiredArgsConstructor
public class NetSuiteClient {

    private final OAuthService oAuthService;

    private final ObjectMapper objectMapper;

    @Getter
    private final String baseUrl;

    private final String realm;

    private final String token;

    private final String tokenSecret;

    public Either<Problem, Optional<String>> retrieveLatestNetsuiteTransactionLines(LocalDate extractionFrom, LocalDate extractionTo) {
        val response = callForTransactionLinesData(extractionFrom, extractionTo);

        if (response.isSuccessful()) {
            log.info("Netsuite response success...customerCode:{}, message:{}", response.getCode(), response.getMessage());
            val body = response.getBody();

            try {
                val bodyJsonTree = objectMapper.readTree(body);
                if (bodyJsonTree.has("error")) {
                    val error = bodyJsonTree.get("error").asInt();
                    val text = bodyJsonTree.get("text").asText();
                    log.error("Error api error:{}, message:{}", error, text);

                    if (error == 105) {
                        log.warn("No data to read from NetSuite API...");

                        return Either.right(Optional.empty());
                    }

                    return Either.left(Problem.builder()
                            .withStatus(Status.valueOf(response.getCode()))
                            .withTitle("NETSUITE_API_ERROR")
                            .withDetail(String.format("Error customerCode: %d, message: %s", error, text))
                            .build());
                }

                return Either.right(Optional.of(response.getBody()));
            } catch (JsonProcessingException e) {
                log.error("Error parsing JSON response from NetSuite API: {}", e.getMessage());

                return Either.left(Problem.builder()
                        .withStatus(Status.valueOf(response.getCode()))
                        .withTitle("NETSUITE_API_ERROR")
                        .withDetail(e.getMessage())
                        .build());
            }
        }

        return Either.left(Problem.builder()
                .withStatus(Status.valueOf(response.getCode()))
                .withTitle("NETSUITE_API_ERROR")
                .withDetail(response.getBody())
                .build());
    }

    private Response callForTransactionLinesData(LocalDate from, LocalDate to) {
        log.info("Retrieving data from NetSuite...");
        log.info("base url: {}", baseUrl);

        val builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("trandate:within", isoFormatDates(from, to));

        val url = builder.toUriString();

        log.info("call url: {}", url);

        val request = new OAuthRequest(GET, url);
        request.setRealm(realm);

        val t = new Token(token, tokenSecret);
        oAuthService.signRequest(t, request);

        return request.send();
    }

    private String isoFormatDates(LocalDate from, LocalDate to) {
        return String.format("%s,%s", ISO_LOCAL_DATE.format(from), ISO_LOCAL_DATE.format(to));
    }

}
