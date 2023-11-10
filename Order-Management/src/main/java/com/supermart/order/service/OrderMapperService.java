package com.supermart.order.service;

import com.supermart.order.dto.OrderItemRequest;
import com.supermart.order.dto.OrderItemResponse;
import com.supermart.order.dto.OrderResponse;
import com.supermart.order.model.Order;
import com.supermart.order.model.OrderItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderMapperService {

    public OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .toList();
        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .orderItems(orderItems)
                .shippingAddress(order.getShippingAddress())
                .paymentStatus(order.getPaymentStatus())
                .createdDate(order.getCreatedDate())
                .build();
    }




    public OrderItem mapToOrderItem(OrderItemRequest orderItemRequest,Double price){
        OrderItem orderItem=new OrderItem();
        orderItem.setSkuCode(orderItemRequest.getSkuCode());
        orderItem.setQuantity(orderItemRequest.getQuantity());
        orderItem.setPrice(price);
        return orderItem;

    }


    public OrderItemResponse mapToOrderItemResponse(OrderItem orderItem){
        return OrderItemResponse.builder()
                .skuCode(orderItem.getSkuCode())
                .quantity(orderItem.getQuantity())
                .pricePerUnit(orderItem.getPrice())
                .build();

    }

    public OrderItemRequest mapToOrderItemRequest(OrderItem orderItem){
        return OrderItemRequest.builder()
                .skuCode(orderItem.getSkuCode())
                .quantity(orderItem.getQuantity())
                .build();

    }

}
