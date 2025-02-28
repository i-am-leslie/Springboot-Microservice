package com.example.orderservice.redisClient;
import com.example.orderservice.DTO.ProductEvent;
import com.example.orderservice.feign.FeignClient;
import com.example.orderservice.model.Product;
import com.example.orderservice.repository.OrderRedisRepository;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Component;


import java.util.List;
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

    enum Action{DELETED, CREATED}



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
        String product1= feignClient.getProductById(productId);
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



//      consuming message and performing logic to the data received
    @Bean
    public Function<String, String> uppercase() {
        return value -> value;  // Transform the data to uppercase
    }

    @Bean
    public Consumer<String> stringConsumer() {
        return message -> {
            try {
                System.out.println("Messaged passed through kafka and is about to be processed by Order service");
                ObjectMapper objectMapper = new ObjectMapper();
                ProductEvent p =objectMapper.readValue(message, ProductEvent.class );
                if(p.getAction().equals(Action.DELETED.toString())){
                    orderRedisRepository.deleteById(p.getPrimaryId());
                }else{
                    System.out.println("Saving to redis");
                    this.getProduct(p.getPrimaryId());
                }

            } catch (Exception e) {
                System.err.println("Error parsing message: " + e.getMessage());
            }
        };
    }
}
