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

    private JsonSchemaMetadataChecker jsonSchemaMetadataChecker;

    @BeforeEach
    public void setUp() {
        val objectMapper = new ObjectMapper();
        val schemaResource = new ClassPathResource("lob_blockchain_transaction_metadata_schema.json");
        jsonSchemaMetadataChecker = new JsonSchemaMetadataChecker(objectMapper);
        jsonSchemaMetadataChecker.setMetatdataSchemaResource(schemaResource);
        jsonSchemaMetadataChecker.setEnableChecker(true);
        jsonSchemaMetadataChecker.init();
    }

    @Test
    public void testCheckTransactionMetadata_Valid() throws IOException {
        // Load valid JSON from test/resources
        val validJson = Files.readString(Path.of("src/test/resources/test_transactions_valid.json"));

        // Check metadata
        val result = jsonSchemaMetadataChecker.checkTransactionMetadata(validJson);

        // Assert that the validation passes
        assertThat(result).isTrue();
    }

    @Test
    public void testCheckTransactionMetadata_Invalid() throws IOException {
        // Load invalid JSON from test/resources
        val invalidJson = Files.readString(Path.of("src/test/resources/test_transactions_invalid.json"));

        // Check metadata
        val result = jsonSchemaMetadataChecker.checkTransactionMetadata(invalidJson);

        // Assert that the validation fails
        assertThat(result).isFalse();
    }

}
