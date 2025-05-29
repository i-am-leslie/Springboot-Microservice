package com.example.orderservice;

import com.example.orderservice.DTO.ProductDetails;
import com.example.orderservice.DTO.StatusChange;
import com.example.orderservice.feign.FeignClient;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.model.Orders;
import com.example.orderservice.model.Product;
import com.example.orderservice.repository.OrderRedisRepository;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@TestConfiguration(proxyBeanMethods = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = {OrderServiceApplication.class, TestContainersConfig.class} )
class OrderServiceApplicationTest {


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderRedisRepository redisCache;

    @MockitoBean
    private FeignClient feignClient;

    @BeforeEach
    void setUp() {
        List<String> productids = new ArrayList<>();
        productids.add("1");
        productids.add("2");
        List<Orders> order=List.of(new Orders("123",productids, OrderStatus.PENDING));
        orderRepository.saveAll(order);

        List<ProductDetails> mockedProductDetails = List.of(
                new ProductDetails("1", "Product 1", 50),
                new ProductDetails("2", "Product 2", 75)
        );

        when(feignClient.getProductById("1")).thenReturn(ResponseEntity.ok("1"));
        Product product=new Product("1",120L,"product",120,"CREATED");
        redisCache.save(product);
    }

    @Test
    void saveOrder() {
        String productId = "1";

        ResponseEntity<String> response = restTemplate
                .postForEntity("/api/v1/Orders/create/{productId}", null, String.class, productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).matches("New order created");
    }

    @Test
    void saveOrderFailed() {
        String productId = "2";

        ResponseEntity<String> response = restTemplate
                .postForEntity("/api/v1/Orders/create/{productId}", null, String.class, productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).matches("Order creation failed");
    }
//
//    @Test
//    void getAllOrder() {
//        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/Orders/all?page=0&size=10", String.class);
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).contains("Product 1", "Product 2");
//
//    }

    @Test
    void deleteOrderTest() {

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/Orders/delete/{orderId}",
                HttpMethod.DELETE,
                request,
                String.class,
                "123"
        );

        // assert status and repository result
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(orderRepository.findById("123")).isEmpty();
    }

    @Test
    void changeOrderStatusTest() {
        StatusChange orderStatus=new StatusChange("123", OrderStatus.DELIVERED);

        ResponseEntity<String> response = restTemplate
                .postForEntity("/api/v1/Orders/change/status", orderStatus, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Orders order=orderRepository.findById("123").get();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);

    }


}