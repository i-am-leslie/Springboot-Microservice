package com.example.orderservice.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductEvent {
    @JsonProperty("action")
    private String action;
    @JsonProperty("primaryId")
    private String primaryId;


    @JsonProperty("correlationId")
    private String correlationId;

    @Override
    public String toString() {
        StringBuilder productEventString= new StringBuilder();
        productEventString.append(action).append(" ").append(primaryId).append(" ").append(" ").append(correlationId);
        return productEventString.toString();
    }





}
