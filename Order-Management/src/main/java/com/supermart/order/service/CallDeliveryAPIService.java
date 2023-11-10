package com.supermart.order.service;

import com.supermart.order.dto.InitiateDeliveryRequest;
import com.supermart.order.dto.OrderItemRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CallDeliveryAPIService {
    private final WebClient.Builder webClientBuilder;
    private final String deliveryMicroServiceUrl="http://DELIVERY-MANAGEMENT/api/delivery/";

    @CircuitBreaker(name = "user",fallbackMethod = "fallbackMethod")
    @Retry(name = "user")
    public boolean initiateDeliveryCall(InitiateDeliveryRequest initiateDeliveryRequest) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(deliveryMicroServiceUrl + "initiate")
                    .bodyValue(initiateDeliveryRequest)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e){
            throw new RuntimeException("Error when making request to delivery service");
        }
    }

    public boolean fallbackMethod(InitiateDeliveryRequest initiateDeliveryRequest, RuntimeException runtimeException){
        throw new RuntimeException("unable to initiate delivery at the moment");
    }
}
