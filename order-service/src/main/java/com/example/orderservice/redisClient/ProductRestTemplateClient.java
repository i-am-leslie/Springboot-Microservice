package com.example.orderservice.redisClient;
import com.example.orderservice.DTO.ProductEvent;
import com.example.orderservice.feign.FeignClient;
import com.example.orderservice.model.Product;
import com.example.orderservice.repository.OrderRedisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@Slf4j
public class ProductRestTemplateClient {

    @Autowired
    @Lazy
    OrderRedisRepository orderRedisRepository;


    @Autowired
    FeignClient feignClient;

    private HashMap<String, EventAction> events;

    enum Action{DELETED, CREATED}



    // O(log n)
    private Product CheckRedisCache(String productId){
        try {
            return orderRedisRepository
                    .findById(productId)
                    .orElse(null);
        }catch (Exception ex){
            System.out.println("Error encountered while trying to retrieve product Exception : "+ " "+ex.getMessage());
            return null;
        }
    }
    private void cacheProductObject(Product product) {
        try {
            orderRedisRepository.save(product);
            System.out.println("Saved product in redis");
        }catch (Exception ex){
            System.out.println("Couldnt save product:" +product.getProductId() +" "+ex.getMessage()+": in redis redis");
        }
    }


    public Product getProduct(String productId){
        Product product = CheckRedisCache(productId);
        if(product!=null){
            System.out.println("i have successfully retrieved product id:" + product.getProductId());
            return product;
        }
        System.out.println("Unable to find product in redis with id:" + " "+productId);
        String product1= feignClient.getProductById(productId); //O (log n)
        if(product1.equals("null")){
            System.out.println("Could not find product in product service database");
            return null;
        }
        System.out.println("Got product from product service database "+ " "+product1);
        product=new Product();
        product.setProductId(product1);
        System.out.println("In getProduct method and set product=" +product.getProductId());
        cacheProductObject(product);
        return product;
    }


    /**
     * Processes messages from Kafka by transforming the raw JSON string into a ProductEvent object.
     * This is a functional method that utilizes a lambda function to deserialize the message.
     * @return A Function that takes a JSON string as input and returns a ProductEvent object.
     *
     * Time Complexity: O(1), as it performs a single deserialization operation and contains a fixed number of values
     * Space Complexity : O(n) fixed number of attributes
     */
    @Bean
    public Function<String, ProductEvent> uppercase() {
        return message -> {
            System.out.println("Messaged passed through kafka, Heading to the consumer function for order service");
            ObjectMapper objectMapper = new ObjectMapper();
            ProductEvent productEvent;
            try {
                productEvent =objectMapper.readValue(message, ProductEvent.class );
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing message: " + e.getMessage());
                throw new RuntimeException(e);
            }
            return productEvent;};
    }

    /**
     * Processes a ProductEvent by applying the appropriate operation based on the event type.
     * Uses a HashMap to store lambda functions that define the behavior for each event type
     *
     * @return A Consumer that takes a ProductEvent and performs an action based on its type
     * *Time Complexity: O(1), as HashMap lookup and function execution are constant-time operations.
     * Space Complexity: O(1), because we have a fixed number of entry (changes coming later)
     */
    @Bean
    public Consumer<ProductEvent> stringConsumer() {
        return ProductEvent -> {
            if (events == null) {
                events = new HashMap<>();
                events.put("DELETED", productId -> {
                    orderRedisRepository.deleteById(productId);
                    System.out.println("Deleted product " + productId);
                });

                events.put("CREATED", productId -> {
                    System.out.println("Saving to redis");
                    Product product = new Product();
                    product.setProductId(productId);
                    cacheProductObject(product);
                });
            }

            Consumer<String> operation=events.get(ProductEvent.getAction());
            if(operation!=null){
                operation.accept(ProductEvent.getPrimaryId());
            }else{
                throw new UnsupportedOperationException("Invalid operation");
            }
        };
    }
}
