package com.example.orderservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.hateoas.RepresentationModel;




import lombok.*;
import org.springframework.data.redis.core.RedisHash;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
@RedisHash("Product")
public class Product extends RepresentationModel<Product> {




    @Id
    String productId;
    String name;
    Integer price;
    String action;

}
