package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceFeignClient;
import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enumtype.OrderStatus;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.ItemService;
import com.innowise.orderservice.service.OrderItemService;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.specificatin.OrderSpecification;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    public OrderResponseDto createOrder(OrderCreateDto orderCreateDto) {
        UserResponseDto userResponseDto = feignClient.getUser(orderCreateDto.userId());
        ItemDto itemDto = orderCreateDto.itemDto();
        Map<Item, BigDecimal> itemsPrice = itemService.getItemsPriceById(itemDto);

        Order order = new Order();
        order.setUserId(userResponseDto.id());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(itemsPrice.values().stream().findFirst().get());
        orderRepository.save(order);

        orderItemService.createOrderItem(
                order,
                itemsPrice.keySet().stream().findFirst().get(),
                itemDto.quantity()
        );

        return orderMapper.toDto(order, userResponseDto);
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        Order order = getOrderEntity(id);
        UserResponseDto userResponseDto = feignClient.getUser(order.getUserId());
        return orderMapper.toDto(order, userResponseDto);
    }

    @Override
    public PageOrderResponseDto searchOrders(SearchOrderDto searchUserDto, PageRequestDto pageRequestDto) {
        Pageable pageable = createPageable(pageRequestDto);
        Specification<Order> specification = orderSpecification.build(searchUserDto);
        Page<Order> searchedUsers = orderRepository.findAll(specification, pageable);
        return createPageOrderResponseDto(searchedUsers);
    }

    @Override
    public PageOrderResponseDto getOrdersByUserId(Long userId, PageRequestDto pageRequestDto) {
        Pageable pageable = createPageable(pageRequestDto);
        Page<Order> orders = orderRepository.findAllByUserIdAndDeleted(userId, false, pageable);
        return createPageOrderResponseDto(orders);
    }

    @Transactional
    @Override
    public OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDto) {
        Order order = getOrderEntity(id);
        orderMapper.updateOrder(orderUpdateDto, order);
        Order savedOrder = orderRepository.save(order);
        UserResponseDto userResponseDto = feignClient.getUser(savedOrder.getUserId());
        return orderMapper.toDto(order, userResponseDto);
    }

    @Transactional
    @Override
    public void deleteOrderById(Long id) {
        Order order = getOrderEntity(id);
        order.setDeleted(true);
        orderRepository.save(order);
    }

    @Override
    public Order getOrderEntity(Long id) {
        return orderRepository.findByIdAndDeleted(id, false).get();
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

    private PageOrderResponseDto createPageOrderResponseDto(Page<Order> searchedOrders) {
        return new PageOrderResponseDto(
                searchedOrders.getContent().stream().map(
                        order -> {
                            UserResponseDto userResponseDto = feignClient.getUser(order.getUserId());
                            return orderMapper.toDto(order, userResponseDto);
                        }).toList(),
                searchedOrders.getPageable().getPageNumber(),
                searchedOrders.getPageable().getPageSize(),
                searchedOrders.getTotalElements(),
                searchedOrders.getTotalPages()
        );
    }
}
