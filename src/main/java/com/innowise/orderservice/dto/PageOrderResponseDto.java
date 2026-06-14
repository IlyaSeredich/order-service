package com.innowise.orderservice.dto;

import java.util.List;

public record PageOrderResponseDto(
        List<OrderResponseDto> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
){
}
