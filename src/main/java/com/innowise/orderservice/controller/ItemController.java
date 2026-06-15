package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.ItemCreateDto;
import com.innowise.orderservice.dto.ItemResponseDto;
import com.innowise.orderservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(
            @RequestBody @Valid ItemCreateDto itemCreateDto) {
        ItemResponseDto itemResponseDto = itemService.createItem(itemCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponseDto);
    }
}
