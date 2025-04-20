package com.example.orderservice.DTO;

import com.example.orderservice.model.OrderStatus;

public record StatusChange(String orderId, OrderStatus status){}
