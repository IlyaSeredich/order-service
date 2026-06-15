package com.innowise.orderservice.dto;

import com.innowise.orderservice.enumtype.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SearchOrderDto(
        LocalDateTime from,
        LocalDateTime to,
        List<OrderStatus> statuses
) {
}
