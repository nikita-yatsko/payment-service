package com.payment.service.service.impl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class RandomNumberClient {

    private final WebClient webClient;
    private final String baseUrl;

    public RandomNumberClient(WebClient.Builder builder,
                           @Value("${spring.external.random.base_url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
    }

    public List<Integer> getRandomNumber() {
        return webClient.get()
                .uri(baseUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Integer>>() {})
                .block();
    }
}
