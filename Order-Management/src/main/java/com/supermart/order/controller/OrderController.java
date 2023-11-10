package com.supermart.order.controller;

import com.supermart.order.dto.*;
import com.supermart.order.model.OrderStatus;
import com.supermart.order.model.PaymentStatus;
import com.supermart.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getAllOrders(){
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Integer id){
        return orderService.getOrderById(id);
    }

    @GetMapping("/order-number/{order-number}")
    public OrderResponse getOrderByOrderNumber(@PathVariable("order-number") String orderNumber){
        return orderService.getOrderByOrderNumber(orderNumber);
    }

    @GetMapping("/customer/{customerId}")
    public List<OrderResponse> getOrdersByCustomer(@PathVariable Integer customerId){
        return orderService.getOrdersByCustomer(customerId);
    }

    @GetMapping("/sku-code/{sku-code}")
    public Set<OrderResponse> getOrdersBySkuCode(@PathVariable("sku-code") String skuCode){
        return orderService.getOrdersBySkuCode(skuCode);
    }

    @GetMapping("/payment/{status}")
    public List<OrderResponse> getOrdersByPaymentStatus(@PathVariable PaymentStatus status){
        return orderService.getOrdersByPaymentStatus(status);
    }

    @GetMapping("/date/{start-date}/{end-date}")
    public List<OrderResponse> getOrdersByCreatedDateBetween(
            @PathVariable("start-date") LocalDate startDate,
            @PathVariable("end-date") LocalDate endDate
    ){
        return orderService.getOrdersByCreatedDateBetween(startDate, endDate);
    }

    @GetMapping("/check/status/{order-number}")
    public OrderStatus checkOrderStatus(@PathVariable("order-number") String orderNumber){
        return orderService.checkOrderStatus(orderNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<String> createOrder(@RequestBody OrderRequest orderRequest){
        return  CompletableFuture.supplyAsync(()-> orderService.createOrder(orderRequest));
    }

    @PatchMapping
    public String updateOrder(@RequestBody UpdateOrderRequest updateOrderRequest){
        return orderService.updateOrder(updateOrderRequest);
    }

    @PatchMapping("/order-status")
    public String updateOrderStatus(@RequestBody UpdateOrderStatusRequest request){
        return orderService.updateOrderStatus(request.getOrderNumber(),request.getNewStatus());
    }

    @PatchMapping("/payment-status")
    public String updatePaymentStatus(@RequestBody UpdatePaymentStatus updatePaymentStatus){
        return orderService.updatePaymentStatus(updatePaymentStatus);
    }

    @PatchMapping("/cancel/{order-number}")
    public String cancelOrder(@PathVariable("order-number") String orderNumber){
        return orderService.cancelOrder(orderNumber);
    }

    @DeleteMapping("/{id}")
    public boolean deleteOrder(@PathVariable Integer id){
        return orderService.forceDeleteOrder(id);
    }

    @GetMapping("/check/customer/{customer-id}")
    public List<String> hasUncompletedOrders(@PathVariable("customer-id") Integer customerId){
        return orderService.hasUncompletedOrders(customerId);
    }

    @PostMapping("/check/product")
    public Set<String> isUsedInOrders(@RequestBody List<String> skuCodes){
        return orderService.isUsedInOrders(skuCodes);
    }

    @PatchMapping("/initiate-delivery/{order-number}")
    public String initiateDelivery(@PathVariable("order-number") String orderNumber){
        return orderService.initiateDelivery(orderNumber);
    }

    @PatchMapping("/complete")
    public String completeOrder(@RequestBody CompleteOrderRequest completeOrderRequest){
        return orderService.completeOrder(completeOrderRequest);
    }

}
