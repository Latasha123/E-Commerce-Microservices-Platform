package com.microservices.inventory.service;

import com.microservices.inventory.repository.InventoryRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepo inventoryRepo;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Test isInStock method")
    void testIsInStock() {
        // Implement your test logic here
        String skuCode = "SKU-TEST-001";
        Integer quantity = 5;

        when(inventoryRepo.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity)).thenReturn(true);

        boolean result = inventoryService.isInStock(skuCode, quantity);

        verify(inventoryRepo , times(1)).existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
        assert(result);
    }

    @Test
    @DisplayName("Test isInStock method when item is not in stock")
    void testInventory_NotInStock(){
        String skuCode = "SKU-TEST-002";
        Integer quantity = 10;

        when(inventoryRepo.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode , quantity)).thenReturn(false);

        boolean result = inventoryService.isInStock(skuCode , quantity);
        assert(!result);

        verify(inventoryRepo , times(1)).existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode , quantity);
    }
}
