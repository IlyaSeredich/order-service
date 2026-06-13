package com.innowise.orderservice.entity;

import com.innowise.orderservice.enumtype.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private BigDecimal totalPrice;
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;
}
