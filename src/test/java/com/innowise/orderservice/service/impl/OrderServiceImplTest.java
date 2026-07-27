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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                OrderServiceImpl.class
        }
)
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;
    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private OrderMapper orderMapper;
    @MockitoBean
    private OrderSpecification orderSpecification;
    @MockitoBean
    private UserServiceFeignClient feignClient;
    @MockitoBean
    private ItemService itemService;
    @MockitoBean
    private OrderItemService orderItemService;

    @Test
    void shouldCreateOrder() {
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";

        ItemDto itemDto = new ItemDto(10L, 2);
        OrderCreateDto createDto = new OrderCreateDto(List.of(itemDto));

        UserResponseDto userResponseDto = UserResponseDto.builder().id(userId).build();

        Item item = new Item();
        item.setId(10L);

        OrderDetailsResponseDto orderResponseDto = new OrderDetailsResponseDto(
                BigDecimal.valueOf(100),
                userResponseDto,
                List.of(new OrderItemResponseDto(10L, null, BigDecimal.valueOf(100), 1)));

        Map<Item, PriceDto> itemsPrice =
                Map.of(item, new PriceDto(1, BigDecimal.valueOf(100)));

        when(feignClient.getUser("Bearer 123",email)).thenReturn(userResponseDto);
        when(itemService.getItemsPrice(any())).thenReturn(itemsPrice);

        OrderDetailsResponseDto result = orderService.createOrder(createDto, email, userId, "123");

        assertEquals(orderResponseDto, result);

        verify(feignClient).getUser("Bearer 123",email);
        verify(orderRepository).save(any());
        verify(orderItemService).createOrderItem(any(Order.class), eq(item), eq(1));
    }

    @Test
    void shouldGetOrderById() {
        Long orderId = 1L;
        UUID userId =  UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);

        UserResponseDto user = UserResponseDto.builder().id(userId).build();

        OrderResponseDto responseDto = OrderResponseDto.builder().id(orderId).build();

        when(orderRepository.findByIdAndDeleted(orderId, false))
                .thenReturn(Optional.of(order));

        when(feignClient.getUser(any(String.class), any()))
                .thenReturn(user);

        when(orderMapper.toDto(order, user))
                .thenReturn(responseDto);

        OrderResponseDto result = orderService.getOrderById(orderId, "test");

        assertEquals(responseDto, result);

        verify(feignClient).getUser(any(String.class), any());
        verify(orderRepository).findByIdAndDeleted(orderId, false);
        verify(orderMapper).toDto(order, user);
    }

    @Test
    void shouldRejectGettingOrderWhenIdNotExists() {

        when(orderRepository.findByIdAndDeleted(99999L, false)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(99999L, "test"));
    }

    @Test
    void shouldUpdateOrder() {
        Long orderId = 1L;
        UUID userId =  UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);

        OrderUpdateDto updateDto =
                new OrderUpdateDto(
                        null,
                        OrderStatus.CREATED,
                        null
                );

        UserResponseDto user = UserResponseDto.builder().id(userId).build();

        OrderResponseDto responseDto = OrderResponseDto.builder().id(orderId).build();

        when(orderRepository.findByIdAndDeleted(orderId, false))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        when(feignClient.getUser(userId, "test"))
                .thenReturn(user);

        when(orderMapper.toDto(order, user))
                .thenReturn(responseDto);

        OrderResponseDto result = orderService.updateOrderById(orderId, updateDto, "test");

        assertEquals(responseDto, result);

        verify(orderMapper).updateOrder(updateDto, order);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldSoftDeleteOrder() {
        Long orderId = 1L;

        Order order = new Order();
        order.setId(orderId);
        order.setDeleted(false);

        when(orderRepository.findByIdAndDeleted(orderId, false))
                .thenReturn(Optional.of(order));

        orderService.deleteOrderById(orderId);

        assertTrue(order.isDeleted());

        verify(orderRepository).save(order);
    }

    @Test
    void shouldReturnOrderEntity() {
        Long orderId = 1L;

        Order order = new Order();

        when(orderRepository.findByIdAndDeleted(orderId, false))
                .thenReturn(Optional.of(order));

        Order result = orderService.getOrderEntity(orderId);

        assertEquals(order, result);
    }

    @Test
    void shouldSearchOrders() {
        SearchOrderDto searchDto =
                new SearchOrderDto(null, null, null);

        PageRequestDto pageRequestDto =
                new PageRequestDto(null, null, null, null);

        Specification<Order> specification =
                (root, query, cb) ->
                cb.and(new ArrayList<>());

        Page<Order> page =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 10),
                        0
                );

        when(orderSpecification.build(searchDto))
                .thenReturn(specification);

        when(orderRepository.findAll(
                eq(specification),
                any(Pageable.class)))
                .thenReturn(page);

        PageOrderResponseDto result =
                orderService.searchOrders(searchDto, pageRequestDto, "test");

        assertNotNull(result);

        verify(orderRepository).findAll(eq(specification), any(Pageable.class));
    }

    @Test
    void shouldGetOrdersByUserId() {
        UUID userId = UUID.randomUUID();
        Long orderId = 10L;

        PageRequestDto pageRequestDto =
                new PageRequestDto(null, null, null, null);

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);

        Page<Order> page =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 10),
                        0
                );

        UserResponseDto user = UserResponseDto.builder().id(userId).build();

        OrderResponseDto responseDto = OrderResponseDto.builder().id(orderId).build();

        when(orderRepository.findAllByUserIdAndDeleted(
                eq(userId),
                eq(false),
                any(Pageable.class)
        )).thenReturn(page);

        when(feignClient.getUser(any(String.class), any(String.class)))
                .thenReturn(user);

        when(orderMapper.toDto(order, user))
                .thenReturn(responseDto);

        PageOrderResponseDto result =
                orderService.getOrdersByUserId(userId, pageRequestDto, "test");

        assertNotNull(result);

        verify(orderRepository)
                .findAllByUserIdAndDeleted(eq(userId), eq(false), any(Pageable.class));
    }
}