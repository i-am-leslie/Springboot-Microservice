package com.example.orderservice.controller;

import com.example.orderservice.model.Orders;
import com.example.orderservice.redisClient.ProductRestTemplateClient;
import com.example.orderservice.service.OrderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping(value="v1/Orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping(value = "/create/{productId}")
    public void saveOrder(@PathVariable("productId") String productId, @RequestBody Orders order) throws TimeoutException {
        orderService.saveOrder(order, productId);
    }

    @PostConstruct
    public void init() {
        // Log to check if ProductService is injected
        System.out.println("orderService bean injected: " + (orderService != null));
    }

    @GetMapping(value = "/orders")
    public Iterable<Orders> getAllOrders() throws TimeoutException {
        return  orderService.getOrders();
    }

    @DeleteMapping(value = "/delete/{orderId}")
    public void deleteOrder(@PathVariable("orderId") String orderId ){
        System.out.println("Removing order"+ " " + orderId);
        orderService.deleteOrder(orderId);

    }
}
