package com.example.orderservice.DTO;

import com.example.orderservice.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record StatusChange(@NotBlank String orderId, @Valid OrderStatus status){}
