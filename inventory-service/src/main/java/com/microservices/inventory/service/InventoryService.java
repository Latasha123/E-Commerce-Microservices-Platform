package com.microservices.inventory.service;

import com.microservices.inventory.repository.InventoryRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryService {

    @Autowired
    private InventoryRepo inventoryRepo;

    public boolean isInStock(String skuCode , Integer quantity) {
        log.info("inside inventory service isInStock method for skuCode: {} and quantity: {}", skuCode, quantity);
        return inventoryRepo.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }
}
