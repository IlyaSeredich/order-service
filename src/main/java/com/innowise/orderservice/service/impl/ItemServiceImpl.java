package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.ItemDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public Map<Item, BigDecimal> getItemsPriceById(ItemDto itemDto) {
        Item item = getItemById(itemDto.id());
        BigDecimal price = item.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity()));
        return Map.of(item, price);
    }

    @Override
    public Item getItemById(Long id) {
        return itemRepository.findById(id).get();
    }
}
