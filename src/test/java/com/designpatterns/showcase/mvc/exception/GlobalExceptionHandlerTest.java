package com.designpatterns.showcase.mvc.exception;

import com.designpatterns.showcase.mvc.controller.ProductController;
import com.designpatterns.showcase.mvc.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Should handle ResourceNotFoundException with proper error response")
    void handleResourceNotFoundException_ShouldReturnErrorResponse() throws Exception {
        when(productService.getProductById(any())).thenThrow(new ResourceNotFoundException("Product", 999L));

        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Product not found")))
                .andExpect(jsonPath("$.path", is("/api/products/999")));
    }

    @Test
    @DisplayName("Should handle InvalidRequestException with proper error response")
    void handleInvalidRequestException_ShouldReturnErrorResponse() throws Exception {
        when(productService.getProductById(any())).thenThrow(new InvalidRequestException("Invalid product data"));

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Invalid product data")))
                .andExpect(jsonPath("$.path", is("/api/products/1")));
    }

    @Test
    @DisplayName("Should handle generic exceptions with proper error response")
    void handleGenericException_ShouldReturnErrorResponse() throws Exception {
        when(productService.getProductById(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")))
                .andExpect(jsonPath("$.path", is("/api/products/1")));
    }

}
