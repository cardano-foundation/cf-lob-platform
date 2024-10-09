package org.cardanofoundation.lob.app.blockchain_reader.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${rest.client.connectTimeout:5000}")
    private int connectTimeoutMillis;

    @Value("${rest.client.readTimeout:5000}")
    private int readTimeout;

    @Bean
    public RestClient restClient(RestClient.Builder builder, ClientHttpRequestFactory clientHttpRequestFactory) {
        return builder.requestFactory(clientHttpRequestFactory).build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMillis);
        factory.setReadTimeout(readTimeout);

        return factory;
    }

}
