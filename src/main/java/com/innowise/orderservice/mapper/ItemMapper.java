package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.ItemCreateDto;
import com.innowise.orderservice.dto.ItemResponseDto;
import com.innowise.orderservice.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toItem(ItemCreateDto itemCreateDto);

    ItemResponseDto toDto(Item item);
}
