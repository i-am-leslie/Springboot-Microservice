package com.example.productservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {
    private String action;       // CREATE, UPDATE, DELETE
    private String primaryId;      // The ID of the affected object
    private ProductRequestDTO productRequestDTO;
    private String correlationId; // To track requests across services
}
