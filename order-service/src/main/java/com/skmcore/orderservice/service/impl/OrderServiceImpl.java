package com.skmcore.orderservice.service.impl;

import com.skmcore.orderservice.dto.OrderRequest;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.event.OrderCreatedEvent;
import com.skmcore.orderservice.exception.ResourceNotFoundException;
import com.skmcore.orderservice.mapper.OrderMapper;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderStatus;
import com.skmcore.orderservice.repository.OrderRepository;
import com.skmcore.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.customerId());
        Order order = orderMapper.toEntity(request);
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(
                saved.getId(),
                saved.getCustomerId(),
                saved.getTotalAmount(),
                Instant.now()
        ));
        log.info("Order created with id: {}", saved.getId());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse updateOrderStatus(UUID id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        log.info("Order {} status updated to {}", id, status);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public void cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled", id);
    }
}
