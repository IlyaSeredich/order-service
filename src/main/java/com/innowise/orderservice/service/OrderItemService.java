package com.innowise.orderservice.service;

import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;

public interface OrderItemService {
    void createOrderItem(Order order, Item item, Integer quantity);
}
