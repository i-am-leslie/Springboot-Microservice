package com.example.orderservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.TimeToLive;
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
public class Product {

    @Id
    String productId;
    @TimeToLive
    private Long expiration;
    String name;
    Integer price;
    String action;

}
