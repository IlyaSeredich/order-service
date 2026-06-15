//package com.innowise.orderservice.controller;
//
//import com.innowise.orderservice.dto.ItemDto;
//import com.innowise.orderservice.dto.OrderCreateDto;
//import com.innowise.orderservice.dto.OrderResponseDto;
//import com.innowise.orderservice.dto.OrderUpdateDto;
//import com.innowise.orderservice.entity.Item;
//import com.innowise.orderservice.enumtype.OrderStatus;
//import com.innowise.orderservice.repository.ItemRepository;
//import com.innowise.orderservice.repository.OrderRepository;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.postgresql.PostgreSQLContainer;
//import tools.jackson.databind.ObjectMapper;
//
//import java.math.BigDecimal;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@ActiveProfiles("local")
//@Testcontainers
//class OrderControllerIT {
//    @Container
//    @ServiceConnection
//    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16")
//            .withDatabaseName("test-db")
//            .withUsername("test-user")
//            .withPassword("test-password");
//
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private ItemRepository itemRepository;
//    @Autowired
//    private OrderRepository orderRepository;
//
//    private static final Long USER_ID = 1L;
//    private static final BigDecimal PRICE = BigDecimal.valueOf(100L);
//    private static final int QUANTITY = 2;
//    private static Integer counter = 1;
//
//    @Test
//    void shouldCreateOrder() throws Exception {
//        Item item = new Item();
//        item.setName("test");
//        item.setPrice(PRICE);
//        Item savedItem = itemRepository.save(item);
//
//        OrderCreateDto orderCreateDto =
//                new OrderCreateDto(USER_ID, new ItemDto(savedItem.getId(), QUANTITY));
//
//        mockMvc.perform(post("/api/orders")
//                        .contentType("application/json")
//                        .content(objectMapper.writeValueAsString(orderCreateDto)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").isNotEmpty())
//                .andExpect(jsonPath("$.status").value("CREATED"))
//                .andExpect(jsonPath("$.totalPrice").isNotEmpty())
//                .andExpect(jsonPath("$.userResponseDto.id").value(USER_ID));
//    }
//
//    @Test
//    void shouldGetOrderById() throws Exception {
//        Long id = createOrder();
//        mockMvc.perform(get("/api/orders/" + id))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").isNotEmpty())
//                .andExpect(jsonPath("$.status").value("CREATED"))
//                .andExpect(jsonPath("$.totalPrice").isNotEmpty())
//                .andExpect(jsonPath("$.userResponseDto.id").value(USER_ID));
//    }
//
//    @Test
//    void shouldRejectGettingOrderWhenIdNotExists() throws Exception {
//        mockMvc.perform(get("/api/orders/" + 999999))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void shouldSearchOrders() throws Exception {
//        createOrder();
//        mockMvc.perform(get("/api/orders")
//                        .param("from", "2026-06-15T00:00:00"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.length()").isNotEmpty());
//    }
//
//    @Test
//    void shouldGetOrdersByUserId() throws Exception {
//        createOrder();
//        mockMvc.perform(get("/api/orders/user/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.length()").isNotEmpty());
//    }
//
//    @Test
//    void shouldUpdateOrder() throws Exception {
//        Long id = createOrder();
//        OrderUpdateDto request =
//                new OrderUpdateDto(null, OrderStatus.DONE, null);
//
//        mockMvc.perform(patch("/api/orders/" + id)
//                        .contentType("application/json")
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").isNotEmpty())
//                .andExpect(jsonPath("$.status").value("DONE"))
//                .andExpect(jsonPath("$.totalPrice").isNotEmpty())
//                .andExpect(jsonPath("$.userResponseDto.id").value(USER_ID));
//    }
//
//    @Test
//    void shouldDeleteOrder() throws Exception {
//        Long id = createOrder();
//        mockMvc.perform(delete("/api/orders/" + id))
//                .andExpect(status().isOk());
//    }
//
//    private Long createOrder() throws Exception {
//        Item item = new Item();
//        item.setName("test" + counter++);
//        item.setPrice(PRICE);
//        Item savedItem = itemRepository.save(item);
//
//        OrderCreateDto orderCreateDto =
//                new OrderCreateDto(USER_ID, new ItemDto(savedItem.getId(), QUANTITY));
//
//        ResultActions perform = mockMvc.perform(post("/api/orders")
//                .contentType("application/json")
//                .content(objectMapper.writeValueAsString(orderCreateDto)));
//
//        String responseBody = perform.andReturn().getResponse().getContentAsString();
//
//        OrderResponseDto responseDto = objectMapper.readValue(responseBody, OrderResponseDto.class);
//
//        return responseDto.id();
//    }
//}