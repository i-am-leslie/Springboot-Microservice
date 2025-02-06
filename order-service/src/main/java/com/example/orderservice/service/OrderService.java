package com.example.orderservice.service;

import com.example.orderservice.feign.FeignClient;
import com.example.orderservice.model.Orders;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.model.Product;
import com.example.orderservice.redisClient.ProductRestTemplateClient;
import com.example.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeoutException;


@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FeignClient feign;

    @Autowired
    private ProductRestTemplateClient redisCache;



//    When an order is created it must have a id  or ids
    @CircuitBreaker(name="order-service",fallbackMethod = "buildFallOrderList")
    @Retry(name = "retryOrderService", fallbackMethod= "buildFallOrderList")
    @RateLimiter(name = "order-service",
            fallbackMethod = "buildFallOrderList")
    public Orders saveOrder(Orders order, String productId) throws TimeoutException{
//        String id= feign.getProductById(productId);
//        System.out.println(id);
        Product p=redisCache.getProduct(productId);
        String id= p.getProductId();
        redisCache.getCachedProducts();
        if (order!=null && id!=null ){
            System.out.println(id);
            order.setOrderId(UUID.randomUUID().toString());
            order.getProductsId().add(productId);
            order.setOrderStatus(OrderStatus.PENDING);
            orderRepository.save(order);
            return order;
        }
        return null;
//        log.info("Order has been placed");
    }

    public void deleteOrder(Orders order){
//        log.info("Deleting order: {}",order.getOrderId());
        orderRepository.delete(order);
    }

    public Optional<Orders> getOrderDetails(String orderId){
        return orderRepository.findById(orderId);

    }

    @CircuitBreaker(name="order-service",fallbackMethod = "getOrdersFallback")
    @Retry(name = "retryOrderService", fallbackMethod= "getOrdersFallback")
    @RateLimiter(name = "order-service",
            fallbackMethod = "getOrdersFallback")
    public Iterable <Orders> getOrders() throws TimeoutException{
        return orderRepository.findAll();

    }

    private Orders buildFallOrderList(Orders order, String productId,Throwable t){
        HashSet<String> fallbackSet=new HashSet<>();
        order.setOrderId("0000000-00-00000");
        order.setProductsId(fallbackSet);
        order.setOrderStatus(
                OrderStatus.CANCELLED);
        return order;
    }

    private Iterable<Orders> getOrdersFallback(Throwable t){
        System.out.println("Fallback for getting orders triggered ");
        List<Orders> orderList = new ArrayList<>(); // Create an empty list
        return orderList; // Return the empty list

    }


}
