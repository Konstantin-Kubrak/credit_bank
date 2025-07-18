package ru.neoflex.kubrak.statement.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class RestClientConfig {

    @Value("${deal.service.base-url}")
    private String baseUrl;

    @Bean
    public RestClient calculatorRestClient(RestClient.Builder builder) {
        return builder.baseUrl(baseUrl).build();
    }
}