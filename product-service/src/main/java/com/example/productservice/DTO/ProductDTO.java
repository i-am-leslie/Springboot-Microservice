package com.example.productservice.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductDTO(@NotBlank String name,

                         @NotBlank String description,

                         @NotBlank String quantityInStock,
                         @NotNull @Positive @Min(1)
                         Integer price) {
}
