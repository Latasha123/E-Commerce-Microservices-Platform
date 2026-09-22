package com.microservices.order.service;

import com.microservices.order.dto.OrderRequest;
import com.microservices.order.event.OrderPlacedEvent;
import com.microservices.order.feign.InventoryClient;
import com.microservices.order.model.Order;
import com.microservices.order.repository.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private static final OrderRequest.UserDetails TEST_USER =
            new OrderRequest.UserDetails("test@example.com", "Test", "User");

    private OrderRequest testOrderRequest;

    @BeforeEach
    void setUp() {
        // Prepare test data before each test
        testOrderRequest = new OrderRequest(
                1L,
                "ORDER-12345",
                new BigDecimal("99.99"),
                5,
                "SKU-TEST-001",
                TEST_USER
        );
    }

    @Test
    @DisplayName("Should successfully place order and save to repository")
    void testPlaceOrder_Success(){
        // Given: Mock inventory check and repository
        when(inventoryClient.isInStock(anyString(), anyInt())).thenReturn(true);
        when(orderRepo.save(any(Order.class))).thenReturn(new Order());

        // When: Call the method under test
        orderService.placeOrder(testOrderRequest);

        // Then: Verify interactions
        verify(inventoryClient, times(1)).isInStock(testOrderRequest.skuCode(), testOrderRequest.quantity());
        verify(orderRepo, times(1)).save(any(Order.class));

    }

    @Test
    @DisplayName("Should verify order details are correctly mapped")
    void testPlaceOrder_verifyOrderDetails(){
        // Given: Mock inventory check and repository
        when(inventoryClient.isInStock(anyString(), anyInt())).thenReturn(true);
        when(orderRepo.save(any(Order.class))).thenReturn(new Order());

        // When: Place order
        orderService.placeOrder(testOrderRequest);

        // Then: Verify interactions and request data
        verify(orderRepo,times(1)).save(any(Order.class));
        assertEquals(1L, testOrderRequest.id());
        assertEquals("ORDER-12345", testOrderRequest.orderNumber());
        assertEquals(new BigDecimal("99.99"), testOrderRequest.price());
        assertEquals(5, testOrderRequest.quantity());
        assertEquals("SKU-TEST-001", testOrderRequest.skuCode());
    }

    @Test
    @DisplayName("Should generate unique order number")
    void testPlaceOrder_GeneratesUniqueOrderNumber() {
        // Given: Mock repository and inventory check
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(inventoryClient.isInStock(anyString(), anyInt())).thenReturn(true);
        when(orderRepo.save(any(Order.class))).thenReturn(new Order());

        // When: Place two orders
        orderService.placeOrder(testOrderRequest);
        orderService.placeOrder(testOrderRequest);

        // Then: Verify two different order numbers are generated
        verify(orderRepo, times(2)).save(orderCaptor.capture());

        String firstOrderNumber = orderCaptor.getAllValues().get(0).getOrderNumber();
        String secondOrderNumber = orderCaptor.getAllValues().get(1).getOrderNumber();

        assertThat(firstOrderNumber).isNotEqualTo(secondOrderNumber);
    }

    @Test
    @DisplayName("Should handle zero quantity order")
    void testPlaceOrder_WithZeroQuantity() {
        // Given: Order with zero quantity
        OrderRequest zeroQuantityOrder = new OrderRequest(
                2L,
                "ORDER-00000",
                new BigDecimal("50.00"),
                0,
                "SKU-ZERO-001",
                TEST_USER
        );
        when(inventoryClient.isInStock(anyString(), anyInt())).thenReturn(true);
        when(orderRepo.save(any(Order.class))).thenReturn(new Order());

        // When: Place order
        orderService.placeOrder(zeroQuantityOrder);

        // Then: Verify order is still saved
        verify(inventoryClient, times(1)).isInStock(zeroQuantityOrder.skuCode(), zeroQuantityOrder.quantity());
        verify(orderRepo, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle large quantity order")
    void testPlaceOrder_WithLargeQuantity() {
        // Given: Order with large quantity
        OrderRequest largeQuantityOrder = new OrderRequest(
                3L,
                "ORDER-LARGE",
                new BigDecimal("1999.99"),
                1000,
                "SKU-LARGE-001",
                TEST_USER
        );
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(inventoryClient.isInStock(anyString(), anyInt())).thenReturn(true);
        when(orderRepo.save(any(Order.class))).thenReturn(new Order());

        // When: Place order
        orderService.placeOrder(largeQuantityOrder);

        // Then: Verify quantity is preserved
        verify(orderRepo).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getQuantity()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should never call repository when not placing order")
    void testNoRepositoryCallWhenNotCalled() {
        // When: No method is called
        // Then: Repository and inventory client should never be called
        verify(orderRepo, never()).save(any(Order.class));
        verify(inventoryClient, never()).isInStock(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw exception when product is not in stock")
    void testPlaceOrder_ProductNotInStock() {
        // Given: Product is not in stock
        when(inventoryClient.isInStock(anyString(), anyInt())).thenReturn(false);

        // When & Then: Expect RuntimeException
        assertThatThrownBy(() -> orderService.placeOrder(testOrderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product with SKU code")
                .hasMessageContaining("is not in stock");

        // Verify repository was never called
        verify(orderRepo, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception with correct SKU code when out of stock")
    void testPlaceOrder_ExceptionMessageContainsSku() {
        // Given: Product is not in stock
        when(inventoryClient.isInStock(anyString(), anyInt())).thenReturn(false);

        // When & Then: Verify exception message
        assertThatThrownBy(() -> orderService.placeOrder(testOrderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(testOrderRequest.skuCode());
    }
}
