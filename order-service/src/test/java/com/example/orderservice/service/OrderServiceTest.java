package com.example.orderservice.service;

import com.example.orderservice.DTO.OrderDTO;
import com.example.orderservice.DTO.ProductDetails;
import com.example.orderservice.DTO.StatusChange;
import com.example.orderservice.feign.FeignClient;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.model.Orders;
import com.example.orderservice.model.Product;
import com.example.orderservice.redisClient.ProductRestTemplateClient;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FeignClient feignClient;

    @Mock
    private  ProductRestTemplateClient redisCache;

    private OrderService orderService;


    @BeforeEach
    void setUp() {
        orderService=new OrderService(orderRepository,redisCache, feignClient);
    }



    @Test
    void saveOrder() throws TimeoutException {
        // Given
        String productId = "12345";
        Product expectedProduct = Product.builder().productId(productId).build();
        when(redisCache.getProduct(productId)).thenReturn(expectedProduct);

        // When
        orderService.saveOrder(productId);

        // Then
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderRepository).save(orderCaptor.capture());

        Orders savedOrder = orderCaptor.getValue();
        assertNotNull(savedOrder.getOrderId());
        assertEquals(OrderStatus.PENDING, savedOrder.getOrderStatus());
        assertTrue(savedOrder.getProductsId().contains(productId));
    }

    @Test
    void NullSaveOrder() throws TimeoutException {
        // Given
        String productId = "";
        Product expectedProduct = null;
        when(redisCache.getProduct(productId)).thenReturn(expectedProduct);

        // When
        boolean result=orderService.saveOrder( "");

        // Then
       assertFalse(result);
    }
    @Test
    void NullProductSaveOrder() throws TimeoutException {
        // Given
        String productId = "";
        Product expectedProduct = null;
        when(redisCache.getProduct(productId)).thenReturn(expectedProduct);

        // When
        boolean result=orderService.saveOrder( "");

        // Then
        assertFalse(result);
    }

    @Test
    void deleteOrder() {
        //Given
        Orders order = Orders.builder().
                orderId("12345").
                productsId(List.of("123","12345")).
                orderStatus(OrderStatus.PENDING).build();
        orderRepository.save(order);

        //When
        orderService.deleteOrder(order.getOrderId());

        //Then
        verify(orderRepository).deleteById(order.getOrderId());

    }

    @Test
    void getOrderDetails() {
    }

    @Test
    void changeOrderStatus() {
        //Given
        StatusChange status= new StatusChange("12345", OrderStatus.DELIVERED);
        Orders order = Orders.builder().
                orderId("12345").
                productsId(List.of("123")).
                orderStatus(OrderStatus.PENDING).build();
        when(orderRepository.findById("12345")).thenReturn(Optional.of(order));

        //When
        orderService.changeOrderStatus(status);

        //Then
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderRepository).save(orderCaptor.capture());
        Orders changedOrderStatus = orderCaptor.getValue();
        assertEquals(OrderStatus.DELIVERED, changedOrderStatus.getOrderStatus());

    }

    @Test
    void getOrders() throws TimeoutException {
        //Given
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = Orders.builder().
                orderId("12345").
                productsId(List.of("123")).
                orderStatus(OrderStatus.PENDING).build();
        List<Orders> ordersList = List.of(order);
        Page<Orders> orderPage = new PageImpl<>(ordersList, pageable, ordersList.size());
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        List<ProductDetails> productDetailsList = new ArrayList<>();
        productDetailsList.add(new ProductDetails("123","Bag", 100));
        when(feignClient.getProductDetails(order.getProductsId())).thenReturn(productDetailsList);

        //When
       List<OrderDTO> listOfProducts=orderService.getOrders(pageable);

        //Then
        assertNotNull(listOfProducts);
        assertEquals(1,listOfProducts.get(0).ProductDetails().size() );
        assertEquals("Bag",listOfProducts.get(0).ProductDetails().getFirst().name());
        assertEquals(100,listOfProducts.get(0).ProductDetails().getFirst().price());
    }
}