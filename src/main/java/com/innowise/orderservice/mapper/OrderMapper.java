package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderCreateDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.dto.OrderUpdateDto;
import com.innowise.orderservice.entity.Order;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toOrder(OrderCreateDto orderCreateDto);
    OrderResponseDto toDto(Order order);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrder(OrderUpdateDto userUpdateDto, @MappingTarget Order order);
}
