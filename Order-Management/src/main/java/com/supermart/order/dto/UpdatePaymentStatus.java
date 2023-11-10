package com.supermart.order.dto;

import com.supermart.order.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentStatus {
    private String orderNumber;
    private PaymentStatus newPaymentStatus;
}
