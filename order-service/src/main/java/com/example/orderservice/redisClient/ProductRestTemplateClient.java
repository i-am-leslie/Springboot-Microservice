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

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Optional;
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

    /**
     *  This method attempts to retrieve the product with its id from redis.
     * @param productId
     * @return  an Optional containing the Product if found, or Optional.empty() if not found or an error occurs
     *
     * Time complexity This operation typically has O(1) time complexity for key-based Redis lookups
     */

    private Optional<Product> checkRedisCache(String productId){
        try {
            return orderRedisRepository
                    .findById(productId);
        }catch (Exception ex){
            log.error(ex.getMessage());
            return  Optional.empty();
        }
    }

    /**
     * Caches the particular product to the  redis repository
     * @param product
     */
    private void cacheProductObject(Product product) {
        try {
            orderRedisRepository.save(product);
            log.info("Saved product in redis");
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
    }

    /**
     * This method attemots to retrieve the product from redis, if not found tries to retrieve from product service
     * @param productId
     * @return the product received from redis and it not found in redis and product service returns null
     */
    public Product getProduct(String productId){
        Optional<Product> product = checkRedisCache(productId);
        if(product.isPresent()){
            log.info("i have successfully retrieved product from redis with id:{}", product.get().getProductId());
            return product.get();
        }
        log.error("Unable to find product in redis with id:{}",productId);
        ResponseEntity<String> restTemplateId;
        Product storeProduct=new Product();
        try{
            restTemplateId= feignClient.getProductById(productId);
            log.info("Retrieved product from product service database {}",restTemplateId.getBody());
            storeProduct.setProductId(restTemplateId.getBody());
            cacheProductObject(storeProduct);
        }catch(feign.FeignException ex){
            log.error("Could not find product in product service database",ex);
            return null;
        }
        return storeProduct;
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
    public Function<String, ProductEvent> productEvent() {
        return message -> {
            log.info(message);
            ObjectMapper objectMapper = new ObjectMapper();
            ProductEvent productEvent;
            try {
                productEvent =objectMapper.readValue(message, ProductEvent.class );
            } catch (JsonProcessingException e) {
                log.error("Error parsing message:{} " , e.getMessage());
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
    public Consumer<ProductEvent> productConsumer() {
        return ProductEvent -> {

            if (events == null) {
                events = new HashMap<>();
                //Deleted
                events.put("DELETED", productEvent -> {
                    orderRedisRepository.deleteById(productEvent.getPrimaryId());
                    log.info("Deleted product: {} ", productEvent.getPrimaryId());
                });

                //Create
                events.put("CREATED", productEvent -> {
                    log.info("Created product: {}", productEvent.getPrimaryId());
                    Product product = Product.builder().productId(productEvent.getPrimaryId()).expiration(120L).build();
                    cacheProductObject(product);
                });
            }

            Consumer<ProductEvent> operation=events.get(ProductEvent.getAction());
            if(operation!=null){
                operation.accept(ProductEvent);
            }else{
                throw new UnsupportedOperationException("Invalid operation");
            }
        };
    }
}
