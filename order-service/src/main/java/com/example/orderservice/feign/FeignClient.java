package com.example.orderservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@org.springframework.cloud.openfeign.FeignClient(value = "PRODUCT-SERVICE")
public interface FeignClient {
    @GetMapping(value = "/v1/product/getProductById/{id}")
    @CircuitBreaker(name="order-service")
    String getProductById(@PathVariable("id") String id);


}
