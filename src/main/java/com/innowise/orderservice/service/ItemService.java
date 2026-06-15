package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.ItemCreateDto;
import com.innowise.orderservice.dto.ItemDto;
import com.innowise.orderservice.dto.ItemResponseDto;
import com.innowise.orderservice.dto.PriceDto;
import com.innowise.orderservice.entity.Item;

import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemResponseDto createItem(ItemCreateDto itemCreateDto);

    Map<Item, PriceDto> getItemsPrice(List<ItemDto> itemDtoList);
    Item getItemById(Long id);
}
