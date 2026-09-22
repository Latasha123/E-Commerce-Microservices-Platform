package com.microservices.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.order.dto.OrderRequest;
import com.microservices.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController Unit Tests")
class OrderControllerTest {

    private static final OrderRequest.UserDetails TEST_USER =
            new OrderRequest.UserDetails("test@example.com", "Test", "User");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("Should successfully place order via POST /order/place")
    void testPlaceOrder_Success() throws Exception {
        // Given: Create order request
        OrderRequest orderRequest = new OrderRequest(
                1L,
                "ORDER-123",
                new BigDecimal("99.99"),
                5,
                "SKU-TEST-001",
                TEST_USER
        );

        // Mock service behavior
        doNothing().when(orderService).placeOrder(any(OrderRequest.class));

        // When & Then: Perform POST request and verify response
        mockMvc.perform(post("/order/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order placed successfully!"));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should return 201 CREATED status code")
    void testPlaceOrder_ReturnsCreatedStatus() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest(
                2L,
                "ORDER-456",
                new BigDecimal("149.99"),
                10,
                "SKU-TEST-002",
                TEST_USER
        );

        doNothing().when(orderService).placeOrder(any(OrderRequest.class));

        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should handle order with large quantity")
    void testPlaceOrder_WithLargeQuantity() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest(
                3L,
                "ORDER-LARGE",
                new BigDecimal("999.99"),
                1000,
                "SKU-LARGE",
                TEST_USER
        );

        doNothing().when(orderService).placeOrder(any(OrderRequest.class));

        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order placed successfully!"));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should accept valid JSON payload")
    void testPlaceOrder_ValidJsonPayload() throws Exception {
        // Given: JSON string
        String jsonPayload = """
                {
                    "id": 4,
                    "orderNumber": "ORDER-789",
                    "price": 299.99,
                    "quantity": 15,
                    "skuCode": "SKU-JSON-001"
                }
                """;

        doNothing().when(orderService).placeOrder(any(OrderRequest.class));

        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should handle order with zero price")
    void testPlaceOrder_WithZeroPrice() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest(
                5L,
                "ORDER-FREE",
                BigDecimal.ZERO,
                1,
                "SKU-FREE",
                TEST_USER
        );

        doNothing().when(orderService).placeOrder(any(OrderRequest.class));

        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 400 when Content-Type is missing")
    void testPlaceOrder_MissingContentType() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest(
                6L,
                "ORDER-NO-CT",
                new BigDecimal("50.00"),
                1,
                "SKU-NO-CT",
                TEST_USER
        );

        // When & Then: Don't set Content-Type header
        mockMvc.perform(post("/order/place")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isUnsupportedMediaType());

        // Service should not be called
        verify(orderService, never()).placeOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when request body is empty")
    void testPlaceOrder_EmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when JSON is malformed")
    void testPlaceOrder_MalformedJson() throws Exception {
        // Given: Invalid JSON
        String malformedJson = "{invalid json}";

        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(OrderRequest.class));
    }
}
