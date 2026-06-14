package com.innowise.orderservice.dto;

import com.innowise.orderservice.enumtype.OrderStatus;

import java.time.LocalDateTime;

public record SearchOrderDto(
        LocalDateTime from,
        LocalDateTime to,
        OrderStatus status
) {
}
