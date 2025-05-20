//package com.example.orderservice.redisClient;
//
//import com.example.orderservice.feign.FeignClient;
//import com.example.orderservice.repository.OrderRedisRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProductRestTemplateClientTestx {
//    @Mock
//    private final OrderRedisRepository orderRedisRepository;
//
//    @Mock
//    private final FeignClient feignClient;
//
//    private ProductRestTemplateClient productRestTemplateClient;
//
//    ProductRestTemplateClientTestx() {
//        productRestTemplateClient=new ProductRestTemplateClient(orderRedisRepository, feignClient);
//    }
//
//    @BeforeEach
//    void setUp() {
//    }
//
//    @Test
//    void getProduct() {
//    }
//
//    @Test
//    void productEvent() {
//    }
//
//    @Test
//    void productConsumer() {
//    }
//}