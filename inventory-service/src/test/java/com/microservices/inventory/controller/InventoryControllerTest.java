package com.microservices.inventory.controller;

import com.microservices.inventory.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class InventoryControllerTest {

    @MockitoBean
    private InventoryService inventoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test isInStock Get endpoint ")
    void testIsInStock() throws Exception {

        when(inventoryService.isInStock("SKU-TEST-001", 5)).thenReturn(true);

        mockMvc.perform(get("/inventory/isInStock")
                .param("skuCode", "SKU-TEST-001")
                .param("quantity", "5"))
                .andExpect(status().isOk());

        verify(inventoryService , times(1)).isInStock("SKU-TEST-001", 5);
    }
}
