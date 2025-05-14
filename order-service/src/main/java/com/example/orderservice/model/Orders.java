package com.example.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name="Orders")
public class Orders {
    @Id
    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    @ElementCollection
    private List<String> productsId = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;



}
