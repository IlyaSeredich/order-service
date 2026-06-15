package com.innowise.orderservice.dto;

public record OrderCreateDto(
        Long userId,
        ItemDto itemDto
) {
}
