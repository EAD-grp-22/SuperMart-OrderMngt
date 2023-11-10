package com.supermart.order.repository;

import com.supermart.order.model.Order;
import com.supermart.order.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Integer> {

    Order findByOrderNumber(String orderNumber);
    List<Order> findAllByCustomerId(Integer customerId);
    List<Order> findAllByPaymentStatus(PaymentStatus paymentStatus);

    List<Order> findAllByCreatedDateBetween(LocalDate startDate, LocalDate endDate);
}
