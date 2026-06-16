package com.skmcore.orderservice.repository;

import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByCustomerId(String customerId);

    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
}
