package com.supermart.order.service;


import com.supermart.order.dto.OrderItemRequest;
import com.supermart.order.dto.UseMultipleResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CallInventoryAPIService {
    private final WebClient.Builder webClientBuilder;
    private final String inventoryMicroServiceUrl="http://INVENTORY-MANAGEMENT/api/inventory/";

    @CircuitBreaker(name = "user",fallbackMethod = "requestFallbackMethod")
    @Retry(name = "user")
    public Mono<UseMultipleResponse> requestOrderItems(List<OrderItemRequest> orderItemRequests) {
        try {
            Mono<UseMultipleResponse> response=webClientBuilder.build()
                    .patch()
                    .uri(inventoryMicroServiceUrl+"use/multiple")
                    .bodyValue(orderItemRequests)
                    .retrieve()
                    .bodyToMono(UseMultipleResponse.class);
            return response;
        } catch (Exception e){
            throw new RuntimeException("Error when making request to inventory service");
        }

    }


    @CircuitBreaker(name = "user",fallbackMethod = "restockFallbackMethod")
    @Retry(name = "user")
    public boolean restockOrderItems(List<OrderItemRequest> orderItemRequests) {
        try {
            boolean response= Boolean.TRUE.equals(webClientBuilder.build()
                    .patch()
                    .uri(inventoryMicroServiceUrl + "restock/multiple")
                    .bodyValue(orderItemRequests)
                    .retrieve()
                    .bodyToMono(Boolean.class).block());
            return response;
        } catch (Exception e){
            throw new RuntimeException("Error when making request to inventory service");
        }

    }

    public Mono<UseMultipleResponse> requestFallbackMethod(List<OrderItemRequest> orderItemRequests, RuntimeException runtimeException){
        throw new RuntimeException("unable to request items at the moment");
    }

    public boolean restockFallbackMethod(List<OrderItemRequest> orderItemRequests, RuntimeException runtimeException){
        throw new RuntimeException("unable to restock items at the moment");
    }


}
