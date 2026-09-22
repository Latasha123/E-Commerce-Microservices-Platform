package com.microservices.order.controller;

import com.microservices.order.dto.OrderRequest;
import com.microservices.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        // Logic to place an order
        orderService.placeOrder(orderRequest);
        return "Order placed successfully!";
    }
}
