package com.example.orderservice.service;

import com.example.orderservice.DTO.OrderDTO;
import com.example.orderservice.DTO.ProductDetails;
import com.example.orderservice.DTO.StatusChange;
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

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


@Service
@Slf4j
public class OrderService {


    private final OrderRepository orderRepository;

    FeignClient feignClient;


    private final ProductRestTemplateClient redisCache; // for caching products to reduce queries for database


    public OrderService(OrderRepository orderRepository,ProductRestTemplateClient redisCache,FeignClient feignClient){
        this.orderRepository=orderRepository;
        this.redisCache=redisCache;
        this.feignClient=feignClient;
    }


//    When an order is created it must have a id  or ids

    /**
     * Saves the given order with the associated product ID. If the product is not found in the Redis cache,
     * it retrieves the product from an external service and caches it. Then, it generates a unique order ID,
     * associates the product with the order, and saves it in the order repository.
     *
     * @param productId The product ID associated with the order.
     * @return True or False
     * @throws TimeoutException If the operation takes longer than expected.
     *
     * Best case time complexity: O(1) when the product is found in the Redis cache, as Redis performs constant
     * time lookups.
     *
     * Worst case time complexity: O(log n) when the product is not found in the cache, requiring a lookup
     * from the product service and saving the order in the database. The database insert is O(log n) due to
     * the time spent indexing the product using a B-tree.
     */

    @CircuitBreaker(name="order-service",fallbackMethod = "failedOrder")
    @Retry(name = "retryOrderService", fallbackMethod= "failedOrder")
    @RateLimiter(name = "order-service",
            fallbackMethod = "failedOrder")
    public boolean saveOrder(String productId) throws TimeoutException{
        Product product=redisCache.getProduct(productId);
        if(product!=null ){
            Orders order=Orders.builder().
                    orderId(UUID.randomUUID().toString()).
                    productsId(new ArrayList<>()).
                    orderStatus(OrderStatus.PENDING).build();
            order.getProductsId().add(product.getProductId());
            orderRepository.save(order);
            log.info("Order saved");
            return true;
        }
        log.error("Failed to create order");
        return false;
    }

    /**
     * This method deletes an order through the id
     * @param orderId
     *
     * Worst case time complexity is O(Log n ) due to it using indexing for hte database
     */
    public void deleteOrder(String orderId){
        orderRepository.deleteById(orderId);
        log.info("Order deleted");
    }

    /**
     * No usage yet
     * @param orderId
     * @return
     */
    public Optional<Orders> getOrderDetails(String orderId){
        return orderRepository.findById(orderId);
    }

    public void changeOrderStatus(StatusChange statusChange){
        Orders order = orderRepository.findById(statusChange.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found in database"));

        order.setOrderStatus(statusChange.status());
        orderRepository.save(order);
    }

    /**
     * Retrieves all orders from the order repository.
     *
     * @return A List  of all orders with the product name, price and total price  .
     * @throws TimeoutException If the operation takes longer than expected.
     * Time complexity: O(n) where 'n' is the number of orders in the database.
     * The `findAll()` method iterates through all records in the database to return the orders.
     */

    @CircuitBreaker(name="order-service",fallbackMethod = "getOrdersFallback")
    @Retry(name = "retryOrderService", fallbackMethod= "getOrdersFallback")
    @RateLimiter(name = "order-service",
            fallbackMethod = "getOrdersFallback")
    public List<OrderDTO> getOrders(Pageable page) throws TimeoutException{
        try {
            List<Orders> orders = orderRepository.findAll(page).getContent();
            Set<String> productIds = new HashSet<>();
            for (Orders order : orders) {
                productIds.addAll(order.getProductsId());
            }

            List<ProductDetails> productDetails = feignClient.getProductDetails(productIds.stream().toList());
            Map<String, ProductDetails> productMap = productDetails.stream()
                    .collect(Collectors.toMap(ProductDetails::id, p -> p));

            List<OrderDTO> orderDTOS = new ArrayList<>();
            for (Orders order : orders) {
                List<ProductDetails> detailsForOrder = order.getProductsId().stream()
                        .map(productMap::get)
                        .filter(Objects::nonNull)
                        .toList();

                int totalPrice = detailsForOrder.stream()
                        .mapToInt(ProductDetails::price)
                        .sum();

                OrderDTO orderDto = new OrderDTO(
                        order.getOrderId(),
                        detailsForOrder,
                        totalPrice,
                        order.getOrderStatus()
                );
                orderDTOS.add(orderDto);
            }

            return orderDTOS;
        } catch (Exception e) {
            log.error("Failed to fetch orders: {}", e.getMessage(), e);
            // fallback: return empty list or propagate a custom exception
            return Collections.emptyList();
        }
    }

    /**
     * Fall back method for resilience to return some data to the user in case an error occurs.
     * @param productId
     * @param t
     * @return order
     */
    private boolean failedOrder(String productId,Throwable t){
        List<String> fallbackSet=new ArrayList<>(); // receives an invalid data
        Orders failedOrder=new Orders();
        failedOrder.setOrderId("0000000-00-00000");
        failedOrder.setProductsId(fallbackSet);
        failedOrder.setOrderStatus(
                OrderStatus.CANCELLED);
        return false;
    }

    private List<Orders>  getOrdersFallback(Throwable t){
        System.out.println("Fallback for getting orders triggered ");
        return new ArrayList<>(); // Return the empty list
    }


}
