package org.cardanofoundation.lob.app.blockchain_common.service_assistance;

import static com.networknt.schema.SpecVersion.VersionFlag.V7;

import java.io.IOException;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonSchemaMetadataChecker implements MetadataChecker {

    private final ObjectMapper objectMapper;

    @Value("classpath:api1_lob_blockchain_transaction_metadata_schema.json")
    @Setter
    protected Resource metadataSchemaResource;

    @Value("${lob.l1.transaction.metadata.validation.enable:true}")
    @Setter
    protected boolean enableChecker;

    @PostConstruct
    public void init() {
        log.info("JsonSchemaMetadataChecker, metadata validation enabled: {}, schema found:{}", enableChecker, metadataSchemaResource.exists());
    }

    @Override
    public boolean checkTransactionMetadata(String json) {
        if (!enableChecker) {
            log.warn("Metadata validation is disabled, not recommended in production / mainnet!");

            return true;
        }

        try {
            val jsonObject = objectMapper.readTree(json);
            val jsonSchemaFactory = JsonSchemaFactory.getInstance(V7);
            val schema = jsonSchemaFactory.getSchema(metadataSchemaResource.getInputStream());
            val validationResult = schema.validate(jsonObject);

            if (!validationResult.isEmpty()) {
                log.error("Metadata validation failed: {}", validationResult);

                return false;
            }

            return true;
        } catch (IOException e) {
            log.error("Error serializing metadata to cbor", e);
            return false;
        }
    }

}
