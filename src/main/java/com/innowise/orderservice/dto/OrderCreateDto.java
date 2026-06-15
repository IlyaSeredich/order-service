package com.innowise.orderservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateDto(
        @NotNull(message = "Items must not be null")
        List<ItemDto> itemDtoList
) {
}
