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

    @Value("")
    private String defaultProcessorUrl;

    @Bean
    public WebClient defaultProcessorWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(500));
        return WebClient.builder()
                .baseUrl(defaultProcessorUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
