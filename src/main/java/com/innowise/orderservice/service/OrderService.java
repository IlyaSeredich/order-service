package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.Order;

import java.util.UUID;

public interface OrderService {
    OrderDetailsResponseDto createOrder(
            OrderCreateDto orderCreateDto,
            String email,
            UUID userId,
            String token
    );

    OrderResponseDto getOrderById(Long id, String token);

    PageOrderResponseDto searchOrders(SearchOrderDto searchUserDto, PageRequestDto pageRequestDto, String token);

    PageOrderResponseDto getOrdersByUserId(UUID userId, PageRequestDto pageRequestDto, String token);

    OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDto, String token);

    void deleteOrderById(Long id);

    Order getOrderEntity(Long id);
}
