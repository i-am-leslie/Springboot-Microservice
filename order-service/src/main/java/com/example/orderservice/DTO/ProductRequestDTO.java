package com.example.orderservice.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductRequestDTO(@NotBlank String name,

                                 String description,

                                String quantityInStock,
                                @NotNull @Positive @Min(1)
                                Integer price) {
}
