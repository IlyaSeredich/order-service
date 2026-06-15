package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderCreateDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.dto.OrderUpdateDto;
import com.innowise.orderservice.dto.UserResponseDto;
import com.innowise.orderservice.entity.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toOrder(OrderCreateDto orderCreateDto);

    @Mapping(source = "order.id", target = "id")
    OrderResponseDto toDto(Order order, UserResponseDto userResponseDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrder(OrderUpdateDto userUpdateDto, @MappingTarget Order order);
}
