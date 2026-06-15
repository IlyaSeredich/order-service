package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.ItemCreateDto;
import com.innowise.orderservice.dto.ItemDto;
import com.innowise.orderservice.dto.ItemResponseDto;
import com.innowise.orderservice.dto.PriceDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemResponseDto createItem(ItemCreateDto itemCreateDto) {
        Item item = itemMapper.toItem(itemCreateDto);
        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    public Map<Item, PriceDto> getItemsPrice(List<ItemDto> itemDtoList) {
        Map<Item, PriceDto> itemMap = new HashMap<>();

        itemDtoList.forEach(itemDto -> {
            Item item = getItemById(itemDto.id());
            BigDecimal price = item.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity()));
            itemMap.put(item, new PriceDto(itemDto.quantity(), price));
        });

        return itemMap;
    }

    @Override
    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }
}
