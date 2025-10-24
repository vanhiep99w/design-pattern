package com.designpatterns.showcase.mvc.controller;

import com.designpatterns.showcase.common.domain.Product;
import com.designpatterns.showcase.common.domain.ProductCategory;
import com.designpatterns.showcase.common.repository.ProductRepository;
import com.designpatterns.showcase.mvc.dto.ProductDTO;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Product Controller Integration Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .category(ProductCategory.ELECTRONICS)
                .stockQuantity(10)
                .available(true)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("GET /api/products - should return all products")
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Product")))
                .andExpect(jsonPath("$[0].price", is(99.99)));
    }

    @Test
    @DisplayName("GET /api/products/{id} - should return product by id")
    void getProductById_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.category", is("ELECTRONICS")));
    }

    @Test
    @DisplayName("GET /api/products/{id} - should return 404 for non-existent product")
    void getProductById_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Product not found")));
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - should return products by category")
    void getProductsByCategory_ShouldReturnFilteredProducts() throws Exception {
        mockMvc.perform(get("/api/products/category/{category}", ProductCategory.ELECTRONICS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("ELECTRONICS")));
    }

    @Test
    @DisplayName("GET /api/products/available - should return available products")
    void getAvailableProducts_ShouldReturnOnlyAvailableProducts() throws Exception {
        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    @Test
    @DisplayName("POST /api/products - should create new product")
    void createProduct_WithValidData_ShouldCreateProduct() throws Exception {
        ProductDTO newProduct = ProductDTO.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .category(ProductCategory.BOOKS)
                .stockQuantity(20)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("New Product")))
                .andExpect(jsonPath("$.price", is(149.99)));
    }

    @Test
    @DisplayName("POST /api/products - should return 400 for invalid data")
    void createProduct_WithInvalidData_ShouldReturn400() throws Exception {
        ProductDTO invalidProduct = ProductDTO.builder()
                .name("")
                .price(new BigDecimal("-10"))
                .stockQuantity(-5)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - should update existing product")
    void updateProduct_WithValidData_ShouldUpdateProduct() throws Exception {
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("199.99"))
                .category(ProductCategory.ELECTRONICS)
                .stockQuantity(15)
                .build();

        mockMvc.perform(put("/api/products/{id}", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Updated Product")))
                .andExpect(jsonPath("$.price", is(199.99)));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - should return 404 for non-existent product")
    void updateProduct_WithInvalidId_ShouldReturn404() throws Exception {
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .price(new BigDecimal("199.99"))
                .category(ProductCategory.ELECTRONICS)
                .stockQuantity(15)
                .build();

        mockMvc.perform(put("/api/products/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - should delete product")
    void deleteProduct_ShouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - should return 404 for non-existent product")
    void deleteProduct_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

}
