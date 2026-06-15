package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import com.innowise.orderservice.repository.OrderItemRepository;
import com.innowise.orderservice.service.OrderItemService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderItemImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;

    @Override
    public void createOrderItem(Order order, Item item, Integer quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setQuantity(quantity);

        orderItemRepository.save(orderItem);
    }
}
