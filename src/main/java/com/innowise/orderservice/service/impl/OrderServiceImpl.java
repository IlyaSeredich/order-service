package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.OrderRepository;
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

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderSpecification orderSpecification;

    @Transactional
    @Override
    public OrderResponseDto createOrder(OrderCreateDto orderCreateDto) {
        Order order = orderMapper.toOrder(orderCreateDto);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        Order order = getOrderEntity(id);
        return orderMapper.toDto(order);
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
        Page<Order> orders = orderRepository.findAllByUserIdAndDeleted(userId,false, pageable);
        return createPageOrderResponseDto(orders);
    }

    @Transactional
    @Override
    public OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDto) {
        Order order = getOrderEntity(id);
        orderMapper.updateOrder(orderUpdateDto, order);
        orderRepository.save(order);
        return orderMapper.toDto(order);
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
                searchedOrders.getContent().stream().map(orderMapper::toDto).toList(),
                searchedOrders.getPageable().getPageNumber(),
                searchedOrders.getPageable().getPageSize(),
                searchedOrders.getTotalElements(),
                searchedOrders.getTotalPages()
        );
    }
}
