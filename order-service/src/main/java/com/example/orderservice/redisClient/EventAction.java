package com.example.orderservice.redisClient;

import com.example.orderservice.DTO.ProductEvent;

import java.util.function.Consumer;

public interface EventAction extends Consumer<ProductEvent> {
}
