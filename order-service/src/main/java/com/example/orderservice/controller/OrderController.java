package com.example.orderservice.controller;

import com.example.orderservice.DTO.StatusChange;
import com.example.orderservice.model.Orders;
import com.example.orderservice.service.OrderService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping(value="api/v1/Orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping(value = "/create/{productId}")
    public ResponseEntity<String> saveOrder(@PathVariable("productId") String productId, @RequestBody Optional<Orders> order) throws TimeoutException {
        if(orderService.saveOrder(order, productId)) return ResponseEntity.ok("New order created");
        return ResponseEntity.ok("Order creation failed");
    }

    @PostConstruct
    public void init() {
        // Log to check if ProductService is injected
        System.out.println("orderService bean injected: " + (orderService != null));
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<Orders>> getAllOrders(@RequestParam(name = "page", defaultValue = "0") int page,
                                                     @RequestParam(name = "size", defaultValue = "10") int size) throws TimeoutException {
        Pageable pageable = PageRequest.of(page, size);
        return  ResponseEntity.ok(orderService.getOrders(pageable));
    }

    @DeleteMapping(value = "/delete/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") String orderId ){
        System.out.println("Removing order"+ " " + orderId);
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("Deleted Product");
    }

    @PostMapping(value="/change/status")
    public ResponseEntity<String> changeOrderStatus(@Valid @RequestBody StatusChange statusChange){
        orderService.changeOrderStatus(statusChange);  // create a dto
        return ResponseEntity.ok("Changed order status");
    }
}
