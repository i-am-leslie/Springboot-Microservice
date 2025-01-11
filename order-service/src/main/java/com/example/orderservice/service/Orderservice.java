package com.example.orderservice.service;

import com.example.orderservice.feign.feignClient;
import com.example.orderservice.model.Orders;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class Orderservice {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private feignClient feign;

//    When an order is creayed it must have a id  or ids
    public void saveOrder(Orders order, String productId){
        String id= feign.getProductById(productId);
        if (order!=null && id!=null ){
            order.setOrderId(UUID.randomUUID().toString());
            order.getProductsId().add(productId);
            order.setOrderStatus(OrderStatus.PENDING);
        }else{
//            log.info("Order is null");
            return;
        }
        orderRepository.save(order);
//        log.info("Order has been placed");
    }

    public void deleteOrg(Orders order){
//        log.info("Deleting order: {}",order.getOrderId());
        orderRepository.delete(order);
    }

    public Optional<Orders> getOrderDetails(String orderId){
        return orderRepository.findById(orderId);

    }

    public Iterable <Orders> getOrders(){
        return orderRepository.findAll();

    }



}
