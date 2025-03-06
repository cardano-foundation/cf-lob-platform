package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.vavr.control.Either;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Slf4j
@RequiredArgsConstructor
public class NetSuiteClient {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Getter
    private final String baseUrl;
    private final String tokenUrl;
    private final String privateKeyFilePath;
    private final String certificateId;
    private final String clientId;

    private String accessToken;
    private LocalDateTime accessTokenExpiration;

    private static final String NETSUITE_API_ERROR = "NETSUITE_API_ERROR";

    @PostConstruct
    public void init() {
        log.info("Initializing NetSuite client...");
        log.info("token url: {}", tokenUrl);

        try {
            refreshToken();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error initializing NetSuite client: {}", e.getMessage());
        }
    }

    private PrivateKey loadPrivateKeyFromFile(String fileName) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File f = new File(fileName);
        String key = Files.readString(f.toPath(), Charset.defaultCharset());

        String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "").replace("-----END PRIVATE KEY-----", "");
        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private String getJwtTokenFromCertifikate() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFilePath);
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setAudience(tokenUrl)
                .setHeader(Map.of("kid", certificateId, "typ", "jwt"))
                .setExpiration(Date.from(Instant.now().plusSeconds(3600))) // 1-hour expiration
                .claim("scope", "restlets")  // Adding the scope claim
                .claim("iss", clientId)  // Adding the issuer (you can adjust it to your needs)
                .signWith(privateKey, SignatureAlgorithm.PS256)  // Sign with RS256
                .compact();
    }

    private void refreshToken() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        log.info("Refreshing NetSuite access token...");
        String jwtToken = getJwtTokenFromCertifikate();
        // Encode parameters
        String requestBody = "grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8)
                + "&client_assertion_type=" + URLEncoder.encode("urn:ietf:params:oauth:client-assertion-type:jwt-bearer", StandardCharsets.UTF_8)
                + "&client_assertion=" + URLEncoder.encode(jwtToken, StandardCharsets.UTF_8);
        // Create the request
        TokenReponse tokenResponse = webClient.post()
                .uri(tokenUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(TokenReponse.class)
                .block();
        accessTokenExpiration = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());
        accessToken = tokenResponse.getAccessToken();
    }

    public Either<Problem, Optional<String>> retrieveLatestNetsuiteTransactionLines(LocalDate extractionFrom, LocalDate extractionTo) {
        ApiResponse response;
        try {
            response = callForTransactionLinesData(extractionFrom, extractionTo);
        } catch (IOException e) {
            return Either.left(Problem.builder()
                    .withStatus(Status.INTERNAL_SERVER_ERROR)
                    .withTitle(NETSUITE_API_ERROR)
                    .withDetail(e.getMessage())
                    .build());
        }

        if (response.isSuccessful()) {
            final String body = response.body();
            log.info("Netsuite response success...customerCode:{}, message:{}", response.status(), body);

            try {
                JsonNode bodyJsonTree = objectMapper.readTree(body);
                if (bodyJsonTree.has("error")) {
                    int error = bodyJsonTree.get("error").asInt();
                    String text = bodyJsonTree.get("text").asText();
                    log.error("Error api error:{}, message:{}", error, text);

                    if (error == 105) {
                        log.warn("No data to read from NetSuite API...");

                        return Either.right(Optional.empty());
                    }

                    return Either.left(Problem.builder()
                            .withStatus(Status.valueOf(response.status()))
                            .withTitle(NETSUITE_API_ERROR)
                            .withDetail(String.format("Error customerCode: %d, message: %s", error, text))
                            .build());
                }

                return Either.right(Optional.of(body));
            } catch (JsonProcessingException e) {
                log.error("Error parsing JSON response from NetSuite API: {}", e.getMessage());

                return Either.left(Problem.builder()
                        .withStatus(Status.valueOf(response.status()))
                        .withTitle(NETSUITE_API_ERROR)
                        .withDetail(e.getMessage())
                        .build());
            }
        }

        return Either.left(Problem.builder()
                .withStatus(Status.valueOf(response.status()))
                .withTitle(NETSUITE_API_ERROR)
                .withDetail(response.body())
                .build());
    }

    private ApiResponse callForTransactionLinesData(LocalDate from, LocalDate to) throws IOException {
        log.info("Retrieving data from NetSuite...");


        if(LocalDate.now().isAfter(ChronoLocalDate.from(accessTokenExpiration))) {
            try {
                refreshToken();
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                log.error("Error refreshing NetSuite access token: {}", e.getMessage());
                return new ApiResponse(HttpStatusCode.valueOf(500).value(), e.getMessage());
            }
        }
        String uriString = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("trandate:within", isoFormatDates(from, to)).toUriString();
        log.info("Call to url: {}", uriString);
        return Objects.requireNonNull(webClient
                .get()
                .uri(uriString)
                .header("Authorization", "Bearer " + accessToken)
                .exchangeToMono(clientResponse -> {
                    int value = clientResponse.statusCode().value();
                    return clientResponse.bodyToMono(String.class).map(body -> new ApiResponse(value, body));
                })
                .block());
    }

    private String isoFormatDates(LocalDate from, LocalDate to) {
        return String.format("%s,%s", ISO_LOCAL_DATE.format(from), ISO_LOCAL_DATE.format(to));
    }

}
