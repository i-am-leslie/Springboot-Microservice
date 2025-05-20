package com.example.orderservice.feign;


import com.example.orderservice.DTO.ProductDetails;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@org.springframework.cloud.openfeign.FeignClient(value = "PRODUCT-SERVICE")
public interface FeignClient {
    @GetMapping(value = "/api/v1/products/getId/{id}")
    ResponseEntity<String> getProductById(@PathVariable("id") String id);

    @PostMapping(value = "/api/v1/products/getProductDetails")
    List<ProductDetails>getProductDetails(@RequestBody List<String> productIds );



}
