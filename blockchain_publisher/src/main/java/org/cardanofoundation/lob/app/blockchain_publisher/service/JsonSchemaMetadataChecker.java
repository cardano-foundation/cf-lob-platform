package org.cardanofoundation.lob.app.blockchain_publisher.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.metadata.MetadataMap;
import com.bloxbean.cardano.client.metadata.helper.MetadataToJsonNoSchemaConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.networknt.schema.SpecVersion.VersionFlag.V7;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonSchemaMetadataChecker implements MetadataChecker {

    @Value("classpath:blockchain_transaction_metadata_schema.json")
    private Resource metatdataSchemaResource;

    @Value("${lob.l1.transaction.metadata.validation.enable:true}")
    private boolean enableChecker;

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        log.info("JsonSchemaMetadataChecker, metadata validation enabled: {}, schema found:{}", enableChecker, metatdataSchemaResource.exists());
    }

    @Override
    public boolean checkTransactionMetadata(MetadataMap metadata) {
        if (!enableChecker) {
            //log.warn("Metadata validation is disabled, not recommended!");

            return true;
        }

        try {
            val data = metadata.getMap();
            val bytes = CborSerializationUtil.serialize(data);

            val json = MetadataToJsonNoSchemaConverter.cborBytesToJson(bytes);

            val jsonObject = objectMapper.readTree(json);

            val jsonSchemaFactory = JsonSchemaFactory.getInstance(V7);

            val schema = jsonSchemaFactory.getSchema(metatdataSchemaResource.getInputStream());

            val validationResult = schema.validate(jsonObject);

            if (!validationResult.isEmpty()) {
                log.error("Metadata validation failed: {}", validationResult);

                return false;
            }

            return true;
        } catch (CborException | IOException e) {
            log.error("Error serializing metadata to cbor", e);
            return false;
        }
    }

}
