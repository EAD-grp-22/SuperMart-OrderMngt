package com.supermart.order.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class CallUserAPIService {
    private final WebClient.Builder webClientBuilder;
    private final String userMicroServiceUrl="http://USER-MANAGEMENT/api/user/auth/validity/{id}";
    @CircuitBreaker(name = "user",fallbackMethod = "fallbackMethod")
    @Retry(name = "user")
    public boolean isCustomerValid(Integer userId) {
        boolean isCustomerValid = Boolean.TRUE.equals(webClientBuilder.build()
                .get()
                .uri(userMicroServiceUrl, userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
        return isCustomerValid;
    }

    public boolean fallbackMethod(Integer userId, RuntimeException runtimeException){
        return false;
    }

}
