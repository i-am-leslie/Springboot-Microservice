package com.example.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Orders")
public class Orders {
    @Id
    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    @ElementCollection
    private Set<String> productsId = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Set<String> getProductsId() {
        return productsId;
    }

    public void setProductsId(Set<String> productsId) {
        this.productsId = productsId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

}
