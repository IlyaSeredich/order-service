package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.PaymentEvent;
import com.innowise.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaServiceImpl {
    private final OrderService orderService;


    @KafkaListener(topics = "${app.kafka.topic}")
    public void consume(PaymentEvent event) {
        orderService.handlePaidOrder(event);
    }
}

