package com.microservices.order.dto;

import java.math.BigDecimal;

public record OrderRequest(Long id , String orderNumber , BigDecimal price , int quantity , String skuCode , UserDetails userDetails) {

    public record UserDetails(String email, String firstName, String lastName) {}
}


