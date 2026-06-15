package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.ItemDto;
import com.innowise.orderservice.entity.Item;

import java.math.BigDecimal;
import java.util.Map;

public interface ItemService {
    Map<Item, BigDecimal> getItemsPriceById(ItemDto itemDto);
    Item getItemById(Long id);
}
