package org.cardanofoundation.lob.app.blockchain_common.service_assistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaMetadataCheckerTest {

    private JsonSchemaMetadataChecker api1JsonSchemaMetadataChecker;
    private JsonSchemaMetadataChecker api3JsonSchemaMetadataChecker;

    @BeforeEach
    public void setUp() {
        val objectMapper = new ObjectMapper();
        val api1SchemaResource = new ClassPathResource("api1_lob_blockchain_transaction_metadata_schema.json");
        val api3SchemaResource = new ClassPathResource("api3_lob_blockchain_transaction_metadata_schema.json");

        api1JsonSchemaMetadataChecker = new JsonSchemaMetadataChecker(objectMapper);
        api1JsonSchemaMetadataChecker.setMetadataSchemaResource(api1SchemaResource);
        api1JsonSchemaMetadataChecker.setEnableChecker(true);
        api1JsonSchemaMetadataChecker.init();

        api3JsonSchemaMetadataChecker = new JsonSchemaMetadataChecker(objectMapper);
        api3JsonSchemaMetadataChecker.setMetadataSchemaResource(api3SchemaResource);
        api3JsonSchemaMetadataChecker.setEnableChecker(true);
        api3JsonSchemaMetadataChecker.init();
    }

    @Test
    public void testAPI1CheckTransactionMetadata_Valid() throws IOException {
        // Load valid JSON from test/resources
        val validJson = Files.readString(Path.of("src/test/resources/api1_test_transactions_valid.json"));

        // Check metadata
        val result = api1JsonSchemaMetadataChecker.checkTransactionMetadata(validJson);

        // Assert that the validation passes
        assertThat(result).isTrue();
    }

    @Test
    public void testAPI1CheckTransactionMetadata_Invalid() throws IOException {
        // Load invalid JSON from test/resources
        val invalidJson = Files.readString(Path.of("src/test/resources/api1_test_transactions_invalid.json"));

        // Check metadata
        val result = api1JsonSchemaMetadataChecker.checkTransactionMetadata(invalidJson);

        // Assert that the validation fails
        assertThat(result).isFalse();
    }

    @Test
    public void testAPI3CheckTransactionMetadata_ValidBalanceSheet() throws IOException {
        // Load valid JSON from test/resources
        val validJson = Files.readString(Path.of("src/test/resources/api3_test_transactions_valid_bs.json"));

        // Check metadata
        val result = api3JsonSchemaMetadataChecker.checkTransactionMetadata(validJson);

        // Assert that the validation passes
        assertThat(result).isTrue();
    }

    @Test
    public void testAPI3CheckTransactionMetadata_IncomeStatement() throws IOException {
        // Load invalid JSON from test/resources
        val invalidJson = Files.readString(Path.of("src/test/resources/api3_test_transactions_valid_is.json"));

        // Check metadata
        val result = api3JsonSchemaMetadataChecker.checkTransactionMetadata(invalidJson);

        // Assert that the validation passes
        assertThat(result).isTrue();
    }

}
