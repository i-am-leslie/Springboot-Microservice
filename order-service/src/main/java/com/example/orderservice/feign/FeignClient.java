package com.example.orderservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@org.springframework.cloud.openfeign.FeignClient(value = "PRODUCT-SERVICE")
public interface FeignClient {
    @GetMapping(value = "/api/v1/products/getId/{id}")
    @CircuitBreaker(name="order-service")
    ResponseEntity<String> getProductById(@PathVariable("id") String id);


}
