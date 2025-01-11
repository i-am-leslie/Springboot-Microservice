package com.example.orderservice.controller;

import com.example.orderservice.model.Orders;
import com.example.orderservice.service.Orderservice;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="v1/Orders")
public class OrderController {
    @Autowired
    private Orderservice orderService;

    @PostMapping(value = "/create/{productId}")
    public void saveOrder(@PathVariable("productId") String productId, @RequestBody Orders order){
        orderService.saveOrder(order, productId);
    }

    @PostConstruct
    public void init() {
        // Log to check if ProductService is injected
        System.out.println("orderService bean injected: " + (orderService != null));
    }

    @GetMapping(value = "/orders")
    public Iterable<Orders> getAllOrders(){
        return  orderService.getOrders();
    }
}
