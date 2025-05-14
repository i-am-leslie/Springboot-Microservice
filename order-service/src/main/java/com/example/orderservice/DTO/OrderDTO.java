package com.example.orderservice.DTO;

import com.example.orderservice.model.OrderStatus;

import java.util.List;

public record OrderDTO(String orderId,
                       List<ProductRequestDTO> productRequestDTOList,
                       int totalPrice,
                       OrderStatus status
                       ) {
}
