package com.innowise.orderservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
public class Item {
    @Id
    private Long id;
    private String name;
    private BigDecimal price;
}
