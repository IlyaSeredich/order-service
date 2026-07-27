package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceFeignClient;
import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enumtype.OrderStatus;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.ItemService;
import com.innowise.orderservice.service.OrderItemService;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.specification.OrderSpecification;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderSpecification orderSpecification;
    private final UserServiceFeignClient feignClient;
    private final ItemService itemService;
    private final OrderItemService orderItemService;

    @Transactional
    @Override
    public OrderDetailsResponseDto createOrder(
            OrderCreateDto orderCreateDto,
            String email,
            UUID userId,
            String token) {

        UserResponseDto userResponseDto = feignClient.getUser("Bearer " + token, email);
        Map<Item, PriceDto> itemsPrice = itemService.getItemsPrice(orderCreateDto.itemDtoList());

        List<OrderItemResponseDto> orderItemResponseDtoList = new ArrayList<>();

        itemsPrice.forEach((k, v) -> {
            Order order = new Order();
            order.setUserId(userResponseDto.id());
            order.setStatus(OrderStatus.CREATED);
            order.setTotalPrice(v.price());
            orderRepository.save(order);

            orderItemResponseDtoList.add(new OrderItemResponseDto(
                    k.getId(),
                    k.getName(),
                    v.price(),
                    v.quantity()));

            orderItemService.createOrderItem(
                    order,
                    k,
                    v.quantity()
            );
        });

        BigDecimal totalOrdersPrice = getOrderPrice(itemsPrice.values());

        return new OrderDetailsResponseDto(totalOrdersPrice, userResponseDto, orderItemResponseDtoList);
    }

    @Override
    public OrderResponseDto getOrderById(Long id, String token) {
        Order order = getOrderEntity(id);
        UserResponseDto userResponseDto =
                feignClient.getUser(order.getUserId(), "Bearer " + token);
        return orderMapper.toDto(order, userResponseDto);
    }

    @Override
    public PageOrderResponseDto searchOrders(SearchOrderDto searchOrderDto, PageRequestDto pageRequestDto, String token) {
        Pageable pageable = createPageable(pageRequestDto);
        Specification<Order> specification = orderSpecification.build(searchOrderDto);
        Page<Order> searchedOrders = orderRepository.findAll(specification, pageable);
        return createPageOrderResponseDto(searchedOrders, token);
    }

    @Override
    public PageOrderResponseDto getOrdersByUserId(UUID userId, PageRequestDto pageRequestDto, String token) {
        Pageable pageable = createPageable(pageRequestDto);
        Page<Order> orders = orderRepository.findAllByUserIdAndDeleted(userId, false, pageable);
        return createPageOrderResponseDto(orders, token);
    }

    @Transactional
    @Override
    public OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDto, String token) {
        Order order = getOrderEntity(id);
        orderMapper.updateOrder(orderUpdateDto, order);
        Order savedOrder = orderRepository.save(order);
        UserResponseDto userResponseDto = feignClient.getUser(savedOrder.getUserId(), "Bearer " + token);
        return orderMapper.toDto(order, userResponseDto);
    }

    @Transactional
    @Override
    public void deleteOrderById(Long id) {
        Order order = getOrderEntity(id);
        orderRepository.delete(order);
//        order.setDeleted(true);
//        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void handlePaidOrder(PaymentEvent paymentEvent) {
        Order order = orderRepository.findById(paymentEvent.orderId()).get();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    @Override
    public Order getOrderEntity(Long id) {
        return orderRepository.findByIdAndDeleted(id, false)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private Pageable createPageable(PageRequestDto pageRequestDto) {
        return PageRequest.of(
                pageRequestDto.pageNumber(),
                pageRequestDto.pageSize(),
                Sort.by(
                        Sort.Direction.fromString(pageRequestDto.sortDirection()),
                        pageRequestDto.sortField()
                ));
    }

    private PageOrderResponseDto createPageOrderResponseDto(Page<Order> searchedOrders, String token) {
        return new PageOrderResponseDto(
                searchedOrders.getContent().stream().map(
                        order -> {
                            UserResponseDto userResponseDto =
                                    feignClient.getUser(order.getUserId(), "Bearer " + token);
                            return orderMapper.toDto(order, userResponseDto);
                        }).toList(),
                searchedOrders.getPageable().getPageNumber(),
                searchedOrders.getPageable().getPageSize(),
                searchedOrders.getTotalElements(),
                searchedOrders.getTotalPages()
        );
    }

    private BigDecimal getOrderPrice(Collection<PriceDto> priceDtoList) {
        BigDecimal orderPrice = new BigDecimal(0);

        for(PriceDto priceDto:priceDtoList) {
            orderPrice = orderPrice.add(priceDto.price());
        }

        return orderPrice;
    }
}
