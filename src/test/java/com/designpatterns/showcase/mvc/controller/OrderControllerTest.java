package com.designpatterns.showcase.mvc.controller;

import com.designpatterns.showcase.common.domain.*;
import com.designpatterns.showcase.common.repository.ProductRepository;
import com.designpatterns.showcase.common.repository.UserRepository;
import com.designpatterns.showcase.mvc.dto.OrderDTO;
import com.designpatterns.showcase.mvc.dto.OrderItemDTO;
import com.designpatterns.showcase.mvc.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Order Controller Integration Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("orderuser")
                .email("order@example.com")
                .firstName("Order")
                .lastName("User")
                .role(UserRole.USER)
                .active(true)
                .build();
        testUser = userRepository.save(testUser);

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("50.00"))
                .category(ProductCategory.ELECTRONICS)
                .stockQuantity(100)
                .available(true)
                .build();
        testProduct = productRepository.save(testProduct);

        testOrder = Order.builder()
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .notes("Test order")
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("GET /api/orders - should return all orders")
    void getAllOrders_ShouldReturnAllOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - should return order by id")
    void getOrderById_ShouldReturnOrder() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testOrder.getId().intValue())))
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - should return 404 for non-existent order")
    void getOrderById_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Order not found")));
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} - should return orders by user")
    void getOrdersByUserId_ShouldReturnUserOrders() throws Exception {
        mockMvc.perform(get("/api/orders/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(testUser.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/orders/status/{status} - should return orders by status")
    void getOrdersByStatus_ShouldReturnFilteredOrders() throws Exception {
        mockMvc.perform(get("/api/orders/status/{status}", OrderStatus.PENDING))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    @DisplayName("POST /api/orders - should create new order")
    void createOrder_WithValidData_ShouldCreateOrder() throws Exception {
        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .build();

        OrderDTO newOrder = OrderDTO.builder()
                .userId(testUser.getId())
                .items(List.of(itemDTO))
                .notes("New test order")
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(100.00)));
    }

    @Test
    @DisplayName("POST /api/orders - should return 400 when order has no items")
    void createOrder_WithNoItems_ShouldReturn400() throws Exception {
        OrderDTO invalidOrder = OrderDTO.builder()
                .userId(testUser.getId())
                .items(List.of())
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("at least one item")));
    }

    @Test
    @DisplayName("POST /api/orders - should return 404 for non-existent user")
    void createOrder_WithInvalidUserId_ShouldReturn404() throws Exception {
        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(1)
                .build();

        OrderDTO invalidOrder = OrderDTO.builder()
                .userId(9999L)
                .items(List.of(itemDTO))
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("POST /api/orders - should return 400 for insufficient stock")
    void createOrder_WithInsufficientStock_ShouldReturn400() throws Exception {
        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(1000)
                .build();

        OrderDTO invalidOrder = OrderDTO.builder()
                .userId(testUser.getId())
                .items(List.of(itemDTO))
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Insufficient stock")));
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - should update order status")
    void updateOrderStatus_ShouldUpdateStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/{id}/status", testOrder.getId())
                        .param("status", OrderStatus.CONFIRMED.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testOrder.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - should return 404 for non-existent order")
    void updateOrderStatus_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(patch("/api/orders/{id}/status", 9999L)
                        .param("status", OrderStatus.CONFIRMED.name()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - should delete order and restore stock")
    void deleteOrder_ShouldDeleteOrderAndRestoreStock() throws Exception {
        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(5)
                .build();

        OrderDTO newOrder = OrderDTO.builder()
                .userId(testUser.getId())
                .items(List.of(itemDTO))
                .build();

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOrder)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDTO createdOrder = objectMapper.readValue(response, OrderDTO.class);
        int stockBeforeDelete = productRepository.findById(testProduct.getId()).get().getStockQuantity();

        mockMvc.perform(delete("/api/orders/{id}", createdOrder.getId()))
                .andExpect(status().isNoContent());

        int stockAfterDelete = productRepository.findById(testProduct.getId()).get().getStockQuantity();
        assert stockAfterDelete == stockBeforeDelete + 5;
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - should return 404 for non-existent order")
    void deleteOrder_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

}
