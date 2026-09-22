package com.example.api_gateway.routes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class Routes {

    @Value("${product.service.url}")
    private String productServiceUrl;
    @Value("${order.service.url}")
    private String orderServiceUrl;
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {

        return GatewayRouterFunctions.route("product-service")
                .route(
                        RequestPredicates.path("/product/**"),
                        HandlerFunctions.http(productServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceCircuitBreaker" , URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productSwagger() {
        return GatewayRouterFunctions.route("product-swagger")
                .route(
                        RequestPredicates.path("/aggregate/product-service/**"),
                        HandlerFunctions.http(productServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceSwaggerCircuitBreaker" , URI.create("forward:/fallbackRoute")))
                .filter(
                        FilterFunctions.rewritePath(
                                "/aggregate/product-service/(?<segment>.*)",
                                "/${segment}"
                        )
                )
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order-service")
                .route(
                        RequestPredicates.path("/order/**"),
                        HandlerFunctions.http(orderServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceCircuitBreaker" , URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderSwagger() {
        return GatewayRouterFunctions.route("order-swagger")
                .route(
                        RequestPredicates.path("/aggregate/order-service/**"),
                        HandlerFunctions.http(orderServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceSwaggerCircuitBreaker" , URI.create("forward:/fallbackRoute")))
                .filter(
                        FilterFunctions.rewritePath(
                                "/aggregate/order-service/(?<segment>.*)",
                                "/${segment}"
                        )
                )
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceRoute() {
        return GatewayRouterFunctions.route("inventory_service")
                .route(
                        RequestPredicates.path("/inventory/**"),
                        HandlerFunctions.http(inventoryServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceCircuitBreaker" , URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventorySwagger() {
        return GatewayRouterFunctions.route("inventory-swagger")
                .route(
                        RequestPredicates.path("/aggregate/inventory-service/**"),
                        HandlerFunctions.http(inventoryServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceSwaggerCircuitBreaker" , URI.create("forward:/fallbackRoute")))
                .filter(
                        FilterFunctions.rewritePath(
                                "/aggregate/inventory-service/(?<segment>.*)",
                                "/${segment}"
                        )
                )
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service Unavailable, please try again later"))
                .build();
    }
}