package com.rinhadebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${payment.processor.default.url}")
    private String defaultProcessorUrl;

    @Value("${payment.processor.fallback.url}")
    private String fallbackProcessorUrl;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.create()
                .responseTimeout(Duration.ofMillis(500))
                .compress(true);
    }

    @Bean
    public WebClient defaultProcessorWebClient(HttpClient httpClient) {
        return WebClient.builder()
                .baseUrl(defaultProcessorUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient fallbackProcessorWebClient(HttpClient httpClient) {
        return WebClient.builder()
                .baseUrl(fallbackProcessorUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
