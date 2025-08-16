package ru.neoflex.kubrak.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${client.service.statement.base-url}")
    private String statementServiceUrl;

    @Value("${client.service.deal.base-url}")
    private String dealServiceUrl;

    @Bean
    public RestClient statementRestClient(RestClient.Builder builder) {
        return builder.baseUrl(statementServiceUrl).build();
    }

    @Bean
    public RestClient dealRestClient(RestClient.Builder builder) {
        return builder.baseUrl(dealServiceUrl).build();
    }
}