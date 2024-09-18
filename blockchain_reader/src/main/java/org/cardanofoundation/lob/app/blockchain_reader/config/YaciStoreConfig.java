package org.cardanofoundation.lob.app.blockchain_reader.config;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class YaciStoreConfig {

    @Value("${lob.blockfrost.url:http://localhost:8080/api/v1/}")
    private String baseUrl;

    @Value("${lob.blockfrost.project_id:}")
    private String projectId;

    @Bean
    @Qualifier("yaci_blockfrost")
    public BackendService yaciBackendService() {
        log.info("Creating Yaci Blockfrost backend service with baseUrl: {}", baseUrl);

        return new BFBackendService(baseUrl, projectId);
    }

}
