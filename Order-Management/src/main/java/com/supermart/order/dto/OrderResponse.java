package com.supermart.order.dto;


import com.supermart.order.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String orderNumber;
    private Integer customerId;
    private String shippingAddress;
    private PaymentStatus paymentStatus;
    private LocalDate createdDate;
    private List<OrderItemResponse> orderItems;
}
