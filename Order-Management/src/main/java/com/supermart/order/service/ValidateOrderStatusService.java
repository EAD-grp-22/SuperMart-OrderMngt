package com.supermart.order.service;

import com.supermart.order.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateOrderStatusService {
    public boolean isStatusUpdateValid(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.CANCELLED;
            case PREPARING:
                return newStatus == OrderStatus.DELIVERYING || newStatus == OrderStatus.CANCELLED;
            case DELIVERYING:
                return newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED;
            default:
                return false;
        }
    }
}
