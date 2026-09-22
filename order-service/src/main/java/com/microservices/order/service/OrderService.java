package com.microservices.order.service;

import com.microservices.order.dto.OrderRequest;
import com.microservices.order.event.OrderPlacedEvent;
import com.microservices.order.feign.InventoryClient;
import com.microservices.order.model.Order;
import com.microservices.order.repository.OrderRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;


    public void placeOrder(OrderRequest orderRequest) {

        boolean isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());
        if (isProductInStock) {
            Order order = new Order();
            order.setId(orderRequest.id());
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setSkuCode(orderRequest.skuCode());
            order.setPrice(orderRequest.price());
            order.setQuantity(orderRequest.quantity());
            orderRepo.save(order);

            // Publish an event to Kafka
            // Match constructor generated from Avro schema fields: orderNumber, email, firstName, lastName
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
                    order.getOrderNumber(),
                    orderRequest.userDetails().email(),
                    orderRequest.userDetails().firstName(),
                    orderRequest.userDetails().lastName()
            );
            log.info("Start Publishing OrderPlacedEvent to Kafka for order number: {}", order.getOrderNumber());
            kafkaTemplate.send("order_placed_topic", "order",orderPlacedEvent);
            log.info("End OrderPlacedEvent published to Kafka for order number: {}", order.getOrderNumber());
        }else{
            throw new RuntimeException("Product with SKU code " + orderRequest.skuCode() + " is not in stock or insufficient quantity available.");
        }
    }
}
