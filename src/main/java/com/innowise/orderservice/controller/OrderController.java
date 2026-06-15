package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/api/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderCreateDto orderCreateDto) {
        OrderResponseDto orderResponseDto = orderService.createOrder(orderCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderResponseDto orderResponseDto = orderService.getOrderById(id);
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping
    public ResponseEntity<PageOrderResponseDto> searchUsers(
            @ModelAttribute SearchOrderDto searchUserDto,
            @ModelAttribute PageRequestDto pageRequestDto
    ) {
        PageOrderResponseDto searchedOrders = orderService.searchOrders(searchUserDto, pageRequestDto);
        return ResponseEntity.ok(searchedOrders);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<PageOrderResponseDto> getOrdersByUserId(
            @PathVariable Long id,
            @ModelAttribute PageRequestDto pageRequestDto
    ) {
        PageOrderResponseDto ordersByUserId = orderService.getOrdersByUserId(id, pageRequestDto);
        return ResponseEntity.ok(ordersByUserId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id,
            @RequestParam OrderUpdateDto orderUpdateDto) {
        OrderResponseDto orderResponseDto = orderService.updateOrderById(id, orderUpdateDto);
        return ResponseEntity.ok(orderResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.ok().build();
    }
}
