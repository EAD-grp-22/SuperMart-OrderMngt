package com.supermart.order.service;

import com.supermart.order.dto.*;
import com.supermart.order.model.Order;
import com.supermart.order.model.OrderItem;
import com.supermart.order.model.OrderStatus;
import com.supermart.order.model.PaymentStatus;
import com.supermart.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderMapperService orderMapperService;
    private final CallUserAPIService callUserAPIService;
    private final CallInventoryAPIService callInventoryAPIService;
    private final ValidateOrderStatusService validateOrderStatusService;

    private final CallDeliveryAPIService callDeliveryAPIService;



    public OrderResponse getOrderById(Integer id){
        Order order = orderRepository.findById(id).get();
        return orderMapperService.mapToOrderResponse(order);
    }

    public List<OrderResponse> getAllOrders(){
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(orderMapperService::mapToOrderResponse).toList();
    }

    public OrderResponse getOrderByOrderNumber(String orderNumber){
        Order order = orderRepository.findByOrderNumber(orderNumber);
        return orderMapperService.mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomer(Integer customerId){
        List<Order> orders = orderRepository.findAllByCustomerId(customerId);
        return orders.stream().map(orderMapperService::mapToOrderResponse).toList();
    }

    public List<OrderResponse> getOrdersByPaymentStatus(PaymentStatus paymentStatus){
        List<Order> orders = orderRepository.findAllByPaymentStatus(paymentStatus);
        return orders.stream().map(orderMapperService::mapToOrderResponse).toList();
    }

    public List<OrderResponse> getOrdersByCreatedDateBetween(LocalDate startDate, LocalDate endDate){
        List<Order> orders = orderRepository.findAllByCreatedDateBetween(startDate, endDate);
        return orders.stream().map(orderMapperService::mapToOrderResponse).toList();
    }

    public Set<OrderResponse> getOrdersBySkuCode(String skuCode){
        List<Order> orders = orderRepository.findAll();
        Set<OrderResponse> matchingOrders = new HashSet<>();
        for (Order order : orders) {
            List<OrderItem> orderItems = order.getOrderItems();
            boolean hasMatchingItem = orderItems.stream()
                    .anyMatch(item -> item.getSkuCode().equals(skuCode));

            if (hasMatchingItem) {
                matchingOrders.add(orderMapperService.mapToOrderResponse(order));
            }
        }
        return matchingOrders;
    }


    public String createOrder(OrderRequest orderRequest){
        if(callUserAPIService.isCustomerValid(orderRequest.getCustomerId())){
                Order order = createOrderFromRequest(orderRequest);
                orderRepository.save(order);
                return "Order Placed Successfully - Order Number:" + order.getOrderNumber();
            }   else {
            throw new RuntimeException("Error when validating customer");
         }
        }

    private Order createOrderFromRequest(OrderRequest orderRequest) {
        UseMultipleResponse response = callInventoryAPIService.requestOrderItems(orderRequest.getOrderItems()).block();
        if (response == null) {
            throw new RuntimeException("Error when using requested items for order");
        }
        String orderNumber = UUID.randomUUID().toString();
        List<OrderItem> orderItems = orderRequest.getOrderItems()
                .stream()
                .map(orderItemRequest -> {
                    Optional<InventoryResponse> inventoryItem = response.getInventoryResponseList().stream()
                            .filter(item -> item.getSkuCode().equals(orderItemRequest.getSkuCode()))
                            .findFirst();

                    if (inventoryItem.isPresent()) {
                        Double price=inventoryItem.get().getPrice();
                        OrderItem orderItem = orderMapperService.mapToOrderItem(orderItemRequest,price);
                        return orderItem;
                    } else {
                        throw new RuntimeException("No matching inventory item found for SKU: " + orderItemRequest.getSkuCode());
                    }
                })
                .toList();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerId(orderRequest.getCustomerId());
        order.setCreatedDate(LocalDate.now());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setOrderItems(orderItems);
        order.setTotalPrice(response.getTotalPrice());
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }
        return order;
    }

    public String cancelOrder(String orderNumber){
        Order existingOrder = orderRepository.findByOrderNumber(orderNumber);
        if(existingOrder != null){
            if(existingOrder.getOrderStatus() == OrderStatus.PENDING){
                List<OrderItem> orderItemsToRestock = existingOrder.getOrderItems();
                List<OrderItemRequest> orderItemRequests = orderItemsToRestock.stream()
                        .map(orderMapperService::mapToOrderItemRequest)
                        .collect(Collectors.toList());
                boolean restocked = callInventoryAPIService.restockOrderItems(orderItemRequests);

                if(restocked){
                    existingOrder.setOrderStatus(OrderStatus.CANCELLED);
                    orderRepository.save(existingOrder);
                    return "Order cancelled successfully";
                } else {
                    throw new RuntimeException("Error when restocking order items.");
                }
            } else {
                return "Order cannot be canceled as it has already been sent for preparation ";
            }
        } else {
            throw new RuntimeException("Order not found with Order No: " + orderNumber);
        }
    }

    public String updateOrderStatus(String orderNumber,OrderStatus newStatus) {
        Order existingOrder = orderRepository.findByOrderNumber(orderNumber);
        if(existingOrder!=null){
            OrderStatus currentStatus = existingOrder.getOrderStatus();
            boolean isValid=validateOrderStatusService.isStatusUpdateValid(currentStatus, newStatus);
            if(isValid){
                existingOrder.setOrderStatus(newStatus);
                orderRepository.save(existingOrder);
                return "Order status updated to " + newStatus.toString();
            } else {
                throw new RuntimeException("Invalid status update. Cannot change order status from " + currentStatus + " to " + newStatus);
            }
        } else {
            throw new RuntimeException("Order not found with Order No: " + orderNumber);
        }
    }

    public String updateOrder(UpdateOrderRequest updateOrderRequest){
        Order existingOrder = orderRepository.findByOrderNumber(updateOrderRequest.getOrderNumber());
        if (existingOrder != null){
            OrderStatus orderStatus=existingOrder.getOrderStatus();
            if(orderStatus != OrderStatus.DELIVERYING && orderStatus != OrderStatus.COMPLETED && orderStatus != OrderStatus.CANCELLED){
                if (updateOrderRequest.getShippingAddress() != null) {
                    existingOrder.setShippingAddress(updateOrderRequest.getShippingAddress());
                    orderRepository.save(existingOrder);
                    return "Order updated successfully";
                }
                else{
                    return "no fields provided for update";
                }
            } else{
                return "Order already prepared, cannot update order details! ";
            }
        } else {
            throw new RuntimeException("Order not found with Order No: " + updateOrderRequest.getOrderNumber());
        }
    }

    public String updatePaymentStatus(UpdatePaymentStatus updatePaymentStatus) {
        Order existingOrder = orderRepository.findByOrderNumber(updatePaymentStatus.getOrderNumber());

        if (existingOrder != null) {
            existingOrder.setPaymentStatus(updatePaymentStatus.getNewPaymentStatus());
            orderRepository.save(existingOrder);
            return "Payment status updated to " + updatePaymentStatus.getNewPaymentStatus() + " for Order Number: " + updatePaymentStatus.getOrderNumber();
        } else {
            throw new RuntimeException("Order not found with Order No: " + updatePaymentStatus.getOrderNumber());
        }
    }



    public boolean forceDeleteOrder(Integer id){
        orderRepository.deleteById(id);
        return true;
    }

    public List<String> hasUncompletedOrders(Integer customerId) {
        List<Order> customerOrders = orderRepository.findAllByCustomerId(customerId);
        List<String> orderNumbers=new ArrayList<>();
        for (Order order : customerOrders) {
            OrderStatus orderStatus = order.getOrderStatus();
            if (orderStatus != OrderStatus.COMPLETED && orderStatus != OrderStatus.CANCELLED) {
                orderNumbers.add(order.getOrderNumber());
            }
        }
        if(!orderNumbers.isEmpty()){
            return orderNumbers;
        } else {
            return null;
        }
    }

    public Set<String> isUsedInOrders(List<String> skuCodes){
        Set<String> usedOrders=new HashSet<>();
        for(String skuCode:skuCodes){
            Set<OrderResponse> orders=getOrdersBySkuCode(skuCode);
            for (OrderResponse order : orders) {
                usedOrders.add(order.getOrderNumber());
            }
        }
        if(!usedOrders.isEmpty()){
            return usedOrders;
        } else{
            return null;
        }
    }

    public OrderStatus checkOrderStatus(String orderNumber){
        Order order=orderRepository.findByOrderNumber(orderNumber);
        return order.getOrderStatus();
    }

    public String initiateDelivery(String orderNumber){
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if(order==null){
            return "Order not found";
        }
        if(order.getOrderStatus()==OrderStatus.PREPARING){
            InitiateDeliveryRequest request = InitiateDeliveryRequest.builder()
                    .orderNumber(order.getOrderNumber())
                    .shippingAddress(order.getShippingAddress())
                    .build();
            boolean isDeliveryInitiated = callDeliveryAPIService.initiateDeliveryCall(request);
            if (isDeliveryInitiated) {
                order.setOrderStatus(OrderStatus.DELIVERYING);
                orderRepository.save(order);
                return "Delivery initiated successfully for Order Number: " + order.getOrderNumber();
            } else {
                throw new RuntimeException("Error when initiating delivery. Please try again.");
            }
        } else if(order.getOrderStatus()==OrderStatus.PENDING) {
            return "Order not ready for delivery";
        } else{
            return "Order delivery cannot be initiated since order has already been delivered,cancelled or in delivery";
        }
    }


    public String completeOrder(CompleteOrderRequest completeOrderRequest) {
        Order existingOrder = orderRepository.findByOrderNumber(completeOrderRequest.getOrderNumber());
        if (existingOrder != null) {
            if(existingOrder.getPaymentStatus()==PaymentStatus.COMPLETED){
                return handleCompleteOrder(existingOrder);
            } else if(completeOrderRequest.isCompletePayment()){
                existingOrder.setPaymentStatus(PaymentStatus.COMPLETED);
                return handleCompleteOrder(existingOrder);
            } else {
                return "Order cannot be completed as it is not currently in the delivery process";
            }
        } else {
            throw new RuntimeException("Order not found with Order No: " + completeOrderRequest.getOrderNumber());
        }
    }

    private String handleCompleteOrder(Order order){
        order.setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        return "Order completed successfully";
    }

}
