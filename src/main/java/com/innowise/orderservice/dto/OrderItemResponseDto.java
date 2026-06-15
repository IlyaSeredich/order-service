package com.innowise.orderservice.dto;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long id,
        String name,
        BigDecimal price,
        Integer quantity
){
}
