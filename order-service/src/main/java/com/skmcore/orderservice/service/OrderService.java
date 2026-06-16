package com.skmcore.orderservice.service;

import com.skmcore.orderservice.dto.OrderRequest;
import com.skmcore.orderservice.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(UUID id);

    List<OrderResponse> getOrdersByCustomer(String customerId);

    OrderResponse updateOrderStatus(UUID id, String status);

    void cancelOrder(UUID id);
}
