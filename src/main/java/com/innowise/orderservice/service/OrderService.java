package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.Order;
import jakarta.transaction.Transactional;

public interface OrderService {
    OrderResponseDto createOrder(OrderCreateDto orderCreateDto);

    OrderResponseDto getOrderById(Long id);

    PageOrderResponseDto searchOrders(SearchOrderDto searchUserDto, PageRequestDto pageRequestDto);

    PageOrderResponseDto getOrdersByUserId(Long userId, PageRequestDto pageRequestDto);

    OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDto);

    void deleteOrderById(Long id);

    Order getOrderEntity(Long id);
}
