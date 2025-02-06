package com.example.productservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Product {
    @Id
    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private  String name;

    @Column
    private String productDescription;

    @Transient
    private String quantityInStock;

    @Column(nullable = false)
    private Integer price;

}
