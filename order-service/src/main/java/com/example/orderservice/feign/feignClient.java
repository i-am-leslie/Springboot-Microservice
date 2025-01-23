package com.example.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "PRODUCT-SERVICE", url = "${product-service.url}")
public interface feignClient {
    @GetMapping(value = "/v1/product/getProductById/{id}")
    String getProductById(@PathVariable("id") String id);
}
