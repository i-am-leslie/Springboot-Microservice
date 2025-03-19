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
    private OrderRepository orderRepository; // storing orders in the database

    @Autowired
    private ProductRestTemplateClient redisCache; // for caching products to reduce queries for database



//    When an order is created it must have a id  or ids

    /**
     * Saves the given order with the associated product ID. If the product is not found in the Redis cache,
     * it retrieves the product from an external service and caches it. Then, it generates a unique order ID,
     * associates the product with the order, and saves it in the order repository.
     *
     * @param order The order to be saved.
     * @param productId The product ID associated with the order.
     * @return The saved order object.
     * @throws TimeoutException If the operation takes longer than expected.
     *
     * Best case time complexity: O(1) when the product is found in the Redis cache, as Redis performs constant
     * time lookups.
     *
     * Worst case time complexity: O(log n) when the product is not found in the cache, requiring a lookup
     * from the product service and saving the order in the database. The database insert is O(log n) due to
     * the time spent indexing the product using a B-tree.
     */

    @CircuitBreaker(name="order-service",fallbackMethod = "buildFallOrderList")
    @Retry(name = "retryOrderService", fallbackMethod= "buildFallOrderList")
    @RateLimiter(name = "order-service",
            fallbackMethod = "buildFallOrderList")
    public Orders saveOrder(Orders order, String productId) throws TimeoutException{
        Product p=redisCache.getProduct(productId);
        String id= p.getProductId();
        System.out.println("In saveOrder method and id="+id);
        if (order!=null && !id.isEmpty() ){
            System.out.println("Saving"+" " +id +"to order repo " );
            System.out.println(id);
            order.setOrderId(UUID.randomUUID().toString());
            order.getProductsId().add(productId);
            order.setOrderStatus(OrderStatus.PENDING);
            orderRepository.save(order);
            return order;
        }
        return null;
    }

    /**
     * This method deletes an order through the id
     * @param orderId
     *
     * Worst case time complexity is O(Log n ) due to it using indexing for hte database
     */
    public void deleteOrder(String orderId){
        orderRepository.deleteById(orderId);
        System.out.println("Deleted order"+" "+orderId);
    }

    public Optional<Orders> getOrderDetails(String orderId){
        return orderRepository.findById(orderId);

    }

    /**
     * Retrieves all orders from the order repository.
     *
     * @return An iterable collection of all orders.
     * @throws TimeoutException If the operation takes longer than expected.
     * Time complexity: O(n) where 'n' is the number of orders in the database.
     * The `findAll()` method iterates through all records in the database to return the orders.
     */

    @CircuitBreaker(name="order-service",fallbackMethod = "getOrdersFallback")
    @Retry(name = "retryOrderService", fallbackMethod= "getOrdersFallback")
    @RateLimiter(name = "order-service",
            fallbackMethod = "getOrdersFallback")
    public Iterable <Orders> getOrders() throws TimeoutException{
        return orderRepository.findAll();
    }

    /**
     * Fall back method for resilience to return some data to the user in case an error occurs.
     * @param order
     * @param productId
     * @param t
     * @return order
     */
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
