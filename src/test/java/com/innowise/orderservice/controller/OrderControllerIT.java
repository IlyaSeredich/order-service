package com.innowise.orderservice.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.enumtype.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@WireMockTest(httpPort = 8088)
class OrderControllerIT {
    @Container
    protected static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.4.5")
            .withAdminUsername("admin")
            .withAdminPassword("admin")
            .withStartupTimeout(Duration.ofMinutes(5));

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("test-db")
            .withUsername("test-user")
            .withPassword("test-password");

    @DynamicPropertySource
    private static void sourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keycloakContainer.getAuthServerUrl() +
                        "/realms/test-realm/protocol/openid-connect/certs");
    }

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderRepository orderRepository;


    private static final BigDecimal PRICE = BigDecimal.valueOf(100.0);
    private static final int QUANTITY = 1;
    private static Integer counter = 1;

    private static final String REALM = "test-realm";
    private static final String USER_CLIENT = "user-test-client";
    private static final String ADMIN_CLIENT = "admin-test-client";
    private static final String SECRET = "test-secret";
    private static final String USER_ROLE = "user";
    private static final String ADMIN_ROLE = "admin";

    @BeforeAll
    static void setupKeycloak() {
        String authUrl = keycloakContainer.getAuthServerUrl();

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authUrl)
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build();

        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(REALM);
        realm.setEnabled(true);
        keycloak.realms().create(realm);

        ClientRepresentation userClient = new ClientRepresentation();
        userClient.setClientId(USER_CLIENT);
        userClient.setStandardFlowEnabled(true);
        userClient.setPublicClient(false);
        userClient.setSecret(SECRET);
        userClient.setServiceAccountsEnabled(true);

        ClientRepresentation adminClient = new ClientRepresentation();
        adminClient.setClientId(ADMIN_CLIENT);
        adminClient.setStandardFlowEnabled(true);
        adminClient.setPublicClient(false);
        adminClient.setSecret(SECRET);
        adminClient.setServiceAccountsEnabled(true);

        Response userResponse = keycloak.realm(REALM).clients().create(userClient);
        Response adminResponse = keycloak.realm(REALM).clients().create(adminClient);

        String userId = CreatedResponseUtil.getCreatedId(userResponse);
        String adminId = CreatedResponseUtil.getCreatedId(adminResponse);

        UserRepresentation userServiceAccountUser = keycloak.realm(REALM)
                .clients()
                .get(userId)
                .getServiceAccountUser();

        UserRepresentation adminServiceAccountUser = keycloak.realm(REALM)
                .clients()
                .get(adminId)
                .getServiceAccountUser();

        RoleRepresentation userRole = new RoleRepresentation();
        userRole.setName(USER_ROLE);
        keycloak.realm(REALM).roles().create(userRole);

        RoleRepresentation adminRole = new RoleRepresentation();
        adminRole.setName(ADMIN_ROLE);
        keycloak.realm(REALM).roles().create(adminRole);

        RoleRepresentation userRoleRepresentation = keycloak.realm(REALM).roles()
                .get(USER_ROLE)
                .toRepresentation();

        RoleRepresentation adminRoleRepresentation = keycloak.realm(REALM).roles()
                .get(ADMIN_ROLE)
                .toRepresentation();

        keycloak.realm(REALM).users()
                .get(userServiceAccountUser.getId())
                .roles()
                .realmLevel()
                .add(List.of(userRoleRepresentation));

        keycloak.realm(REALM).users()
                .get(adminServiceAccountUser.getId())
                .roles()
                .realmLevel()
                .add(List.of(adminRoleRepresentation));
    }

    @AfterEach
    void cleanDb() {
        itemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void shouldCreateOrder() throws Exception {
        Item item = new Item();
        item.setName("test");
        item.setPrice(PRICE);
        Item savedItem = itemRepository.saveAndFlush(item);

        String token = getUserAccessToken();

        OrderCreateDto orderCreateDto =
                new OrderCreateDto(List.of(new ItemDto(savedItem.getId(), QUANTITY)));

        mockGettingUserByEmail();

        mockMvc.perform(post("/api/orders")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(PRICE))
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andDo(print());
    }

    @Test
    void shouldGetOrderById() throws Exception {
        Long id = createOrder();

        String token = getAdminAccessToken();
        String userAccessToken = getUserAccessToken();
        String userId = extractSubject(userAccessToken);

        mockGettingUserById();

        mockMvc.perform(get("/api/orders/" + id)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalPrice").isNotEmpty())
                .andExpect(jsonPath("$.userResponseDto.id").value(userId));
    }

    @Test
    void shouldRejectGettingOrderWhenIdNotExists() throws Exception {
        String token = getAdminAccessToken();

        mockMvc.perform(get("/api/orders/" + 999999)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchOrders() throws Exception {
        createOrder();
        String token = getAdminAccessToken();

        mockGettingUserById();

        mockMvc.perform(get("/api/orders")
                        .param("from", "2026-06-15T00:00:00")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").isNotEmpty());
    }

    @Test
    void shouldGetOrdersByUserId() throws Exception {
        createOrder();

        String token = getAdminAccessToken();
        String id = extractSubject(token);

        mockGettingUserById();

        mockMvc.perform(get("/api/orders/user/" + id)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").isNotEmpty());
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Long id = createOrder();
        OrderUpdateDto request =
                new OrderUpdateDto(null, OrderStatus.IN_PROGRESS, null);

        String token = getUserAccessToken();
        String userId = extractSubject(token);

        mockGettingUserById();

        mockMvc.perform(patch("/api/orders/" + id)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.totalPrice").isNotEmpty())
                .andExpect(jsonPath("$.userResponseDto.id").value(userId));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Long id = createOrder();
        String token = getAdminAccessToken();

        mockMvc.perform(delete("/api/orders/" + id)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk());
    }

    private Long createOrder() throws Exception {
        Item item = new Item();
        item.setName("test" + counter++);
        item.setPrice(PRICE);
        Item savedItem = itemRepository.save(item);

        String token = getUserAccessToken();
        String userId = extractSubject(token);

        OrderCreateDto orderCreateDto =
                new OrderCreateDto(List.of(new ItemDto(savedItem.getId(), QUANTITY)));

        mockGettingUserByEmail();

        ResultActions perform = mockMvc.perform(post("/api/orders")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(orderCreateDto)));

        String responseBody = perform.andReturn().getResponse().getContentAsString();

        OrderDetailsResponseDto responseDto = objectMapper.readValue(responseBody, OrderDetailsResponseDto.class);

        return responseDto.items().getFirst().id();
    }

    private String getUserAccessToken() {
        String tokenUrl = keycloakContainer.getAuthServerUrl()
                + "/realms/test-realm/protocol/openid-connect/token";

        Map<String, String> params = new HashMap<>();
        params.put("realm", REALM);
        params.put("client_id", USER_CLIENT);
        params.put("client_secret", SECRET);
        params.put("grant_type", OAuth2Constants.CLIENT_CREDENTIALS);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = new RestTemplate().postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    private String extractSubject(String token) {
        String payload = token.split("\\.")[1];

        byte[] decoded = Base64.getUrlDecoder()
                .decode(payload);

        try {
            Map<String, Object> claims = objectMapper.readValue(decoded, Map.class);

            return (String) claims.get("sub");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getAdminAccessToken() {
        String tokenUrl = keycloakContainer.getAuthServerUrl()
                + "/realms/test-realm/protocol/openid-connect/token";

        Map<String, String> params = new HashMap<>();
        params.put("realm", "test-realm");
        params.put("client_id", "admin-test-client");
        params.put("client_secret", SECRET);
        params.put("grant_type", OAuth2Constants.CLIENT_CREDENTIALS);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = new RestTemplate().postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    void mockGettingUserById() {
        String userAccessToken = getUserAccessToken();
        String userId = extractSubject(userAccessToken);

        com.github.tomakehurst.wiremock.client.WireMock.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.get(
                                com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/api/users/" + userId))
                        .willReturn(
                                com.github.tomakehurst.wiremock.client.WireMock.okJson("""
                                {
                                  "id":"%s",
                                  "name":"Ilya",
                                  "surname":"Ivanov",
                                  "birthDate":"1998-04-15",
                                  "email":"ilya@test.com",
                                  "active":true,
                                  "paymentCards":[]
                                }
                                """.formatted(userId))));
    }

    void mockGettingUserByEmail() {
        String userAccessToken = getUserAccessToken();
        String userId = extractSubject(userAccessToken);

        com.github.tomakehurst.wiremock.client.WireMock.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.get(
                                com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/api/users/email"))
                        .willReturn(
                                com.github.tomakehurst.wiremock.client.WireMock.okJson("""
                                {
                                  "id":"%s",
                                  "name":"Ilya",
                                  "surname":"Ivanov",
                                  "birthDate":"1998-04-15",
                                  "email":"ilya@test.com",
                                  "active":true,
                                  "paymentCards":[]
                                }
                                """.formatted(userId))));
    }
}